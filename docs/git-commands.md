# Git Commands Reference

A practical list of commonly needed Git commands for daily development.

## 1. Initial setup

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global init.defaultBranch main
```

## 2. Start a repository

```bash
# Initialize in current folder
git init

# Clone existing repository
git clone <repo-url>
```

## 3. Check current state

```bash
git status
git branch
git branch -a
git remote -v
```

## 4. Stage and commit changes

```bash
# Stage one file
git add <file>

# Stage all changes
git add .

# Commit staged changes
git commit -m "Describe your change"

# Amend latest commit message/content
git commit --amend
```

## 5. Push and pull

```bash
# First push for a new branch
git push -u origin <branch-name>

# Push next commits
git push

# Pull and merge
git pull

# Pull with rebase
git pull --rebase

# Fetch without merging
git fetch
```

## 6. Branching

```bash
# Create and switch to new branch
git checkout -b <branch-name>

# Switch branch (new style)
git switch <branch-name>

# Create branch only
git branch <branch-name>

# Delete local branch
git branch -d <branch-name>

# Force delete local branch
git branch -D <branch-name>
```

## 7. Merge and rebase

```bash
# Merge branch into current branch
git merge <branch-name>

# Rebase current branch onto another
git rebase <branch-name>

# Continue after conflict resolution
git rebase --continue

# Abort ongoing rebase
git rebase --abort
```

## 8. View history and differences

```bash
git log
git log --oneline --graph --decorate --all
git diff
git diff --staged
git show <commit-hash>
```

## 9. Undo and recovery

```bash
# Unstage file
git restore --staged <file>

# Discard working directory changes in file
git restore <file>

# Revert commit safely (creates new commit)
git revert <commit-hash>

# Move HEAD (careful)
git reset --soft <commit-hash>
git reset --mixed <commit-hash>
git reset --hard <commit-hash>
```

## 10. Stash temporary work

```bash
# Save uncommitted changes
git stash

# Save with message
git stash push -m "WIP: short note"

# List stashes
git stash list

# Re-apply latest stash and remove from stash list
git stash pop

# Re-apply specific stash
git stash apply stash@{0}
```

## 11. Tags

```bash
# Create lightweight tag
git tag v1.0.0

# Create annotated tag
git tag -a v1.0.0 -m "Release v1.0.0"

# Push one tag
git push origin v1.0.0

# Push all tags
git push origin --tags
```

## 12. Useful cleanup commands

```bash
# Remove untracked files/folders (preview)
git clean -n

# Remove untracked files/folders (execute)
git clean -fd

# Prune deleted remote branches
git fetch --prune
```

## 13. Quick daily workflow

```bash
git pull --rebase
git checkout -b feature/my-change
git add .
git commit -m "Implement my change"
git push -u origin feature/my-change
```

## Notes

- Prefer small, focused commits.
- Use clear commit messages.
- Avoid git reset --hard unless you are sure.
