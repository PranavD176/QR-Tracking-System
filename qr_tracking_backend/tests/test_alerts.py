def test_get_alerts(client, token):
    response = client.get(
        "/alerts",
        headers={"Authorization": f"Bearer {token}"}
    )
    assert response.status_code == 200