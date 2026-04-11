# To run this test, you need to have a Supabase database set up
# venv\Scripts\python tests\test_supabase_integration.py
# a fake user is added and deleted in datdbase 
# result: OK -> all working fine
# if there is an user with email "live_integration_test@example.com" it will be deleted after the test
# if the test fails, the user will not be deleted -> so delete it manually


import unittest
import os
import sys
from dotenv import load_dotenv

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Add parent directory to path so we can import app modules
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from app.database import Base
from app.models.user import User

# Load environment variables
load_dotenv()

# Prioritize a distinct TEST_DATABASE_URL. If missing, fall back to DATABASE_URL.
# IMPORTANT: For live tests against Postgres/Supabase, make sure this points to 
# a safe testing instance or that you rely entirely on the transaction rollbacks.
DATABASE_URL = os.getenv("TEST_DATABASE_URL", os.getenv("DATABASE_URL"))

class TestSupabaseIntegration(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        """
        Runs once before any tests start.
        Initializes the engine and ensures tables exist.
        """
        if not DATABASE_URL or DATABASE_URL.startswith("sqlite"):
            raise unittest.SkipTest("Skipping Supabase integration tests: DATABASE_URL is missing or pointing to local SQLite. Please provide a postgresql:// URL.")
        
        # We need to make sure we are connecting to a valid Postgres database
        cls.engine = create_engine(DATABASE_URL)
        
        # Ensure all tables have been generated in the connected database
        # (If they already exist, this simply does nothing)
        Base.metadata.create_all(cls.engine)
        cls.Session = sessionmaker(bind=cls.engine)

    def setUp(self):
        """
        Runs before EACH test.
        Setting up a SAFE transactional scope. Any database writes happening in this 
        test will be rolled back in tearDown, so the database remains completely untouched.
        """
        # Connect to the database
        self.connection = self.engine.connect()
        
        # Begin a non-ORM transaction
        self.transaction = self.connection.begin()
        
        # Bind an individual session to that connection
        self.session = self.Session(bind=self.connection)

    def tearDown(self):
        """
        Runs after EACH test. 
        Rolls back all changes, guaranteeing your Supabase database stays completely unaltered.
        """
        self.session.close()
        
        # Roll back the transaction. Any INSERT/UPDATE/DELETE run within the test is wiped.
        self.transaction.rollback()
        
        # Return connection to the pool
        self.connection.close()

    def test_live_supabase_insert_and_query(self):
        """Test inserting a user into live Supabase database."""
        
        # 1. Create a dummy test user
        dummy_email = "live_integration_test@example.com"
        new_user = User(
            email=dummy_email,
            full_name="Supabase Integration User",
            hashed_password="securepasswordhash",
            role="user"
        )
        
        # 2. Insert into the database
        self.session.add(new_user)
        self.session.commit() # Commits to the transaction block, not the permanent database!
        
        # 3. Retrieve from the database
        retrieved_user = self.session.query(User).filter_by(email=dummy_email).first()
        
        self.assertIsNotNone(retrieved_user, "Failed to retrieve the inserted user from Supabase.")
        self.assertEqual(retrieved_user.full_name, "Supabase Integration User")
        
        # Once test ends, tearDown automatically rolls back this user so it's gone from Supabase forever.

if __name__ == "__main__":
    unittest.main()
