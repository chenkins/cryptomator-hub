-- noinspection SqlNoDataSourceInspectionForFile

-- This test data can be used in unit tests. A test should leave this data as it found it, either by transaction rollback or other means. See https://github.com/cryptomator/hub/pull/182 for some options

UPDATE "settings"
SET "hub_id" = '42',
	"license_key"  = 'eyJhbGciOiJFUzUxMiJ9.eyJqdGkiOiI0MiIsImlhdCI6MTY0ODA0OTM2MCwiaXNzIjoiU2t5bWF0aWMiLCJhdWQiOiJDcnlwdG9tYXRvciBIdWIiLCJzdWIiOiJodWJAY3J5cHRvbWF0b3Iub3JnIiwic2VhdHMiOjUsImV4cCI6MjUzNDAyMjE0NDAwLCJyZWZyZXNoVXJsIjoiaHR0cDovL2xvY2FsaG9zdDo4Nzg3L2h1Yi9zdWJzY3JpcHRpb24_aHViX2lkPTQyIn0.AKyoZ0WQ8xhs8vPymWPHCsc6ch6pZpfxBcrF5QjVLSQVnYz2s5QF3nnkwn4AGR7V14TuhkJMZLUZxMdQAYLyL95sAV2Fu0E4-e1v3IVKlNKtze89eqYvEs6Ak9jWjtecOgPWNWjz2itI4MfJBDmbFtTnehOtqRqUdsDoC9NFik2C7tHm'
WHERE "id" = 0;

INSERT INTO "authority" ("id", "type", "name")
VALUES
	('user1', 'USER', 'User Name 1'),
	('user2', 'USER', 'User Name 2'),
	('group1', 'GROUP', 'Group Name 1'),
    ('group2', 'GROUP', 'Group Name 2');

INSERT INTO "user_details" ("id")
VALUES
	('user1'),
	('user2');

INSERT INTO "group_details" ("id")
VALUES
	('group1'),
	('group2');

INSERT INTO "group_membership" ("group_id", "member_id")
VALUES
	('group1', 'user1'),
	('group2', 'user2');

INSERT INTO "vault" ("id", "name", "description", "creation_time", "salt", "iterations", "masterkey", "auth_pubkey", "auth_prvkey", "archived")
VALUES
	('7E57C0DE-0000-4000-8000-000100001111', 'Vault 1', 'This is a testvault.', '2020-02-20 20:20:20', 'salt1', 42, 'masterkey1',
	 'MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAElS+JW3VaBvVr9GKZGn1399WDTd61Q9fwQMmZuBGAYPdl/rWk705QY6WhlmbokmEVva/mEHSoNQ98wFm9FBCqzh45IGd/DGwZ04Xhi5ah+1bKbkVhtds8nZtHRdSJokYp',
	 'MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDAa57e0Q/KAqmIVOVcWX7b+Sm5YVNRUx8W7nc4wk1IBj2QJmsj+MeShQRHG4ozTE9KhZANiAASVL4lbdVoG9Wv0YpkafXf31YNN3rVD1/BAyZm4EYBg92X+taTvTlBjpaGWZuiSYRW9r+YQdKg1D3zAWb0UEKrOHjkgZ38MbBnTheGLlqH7VspuRWG12zydm0dF1ImiRik=',
	 FALSE),
	('7E57C0DE-0000-4000-8000-000100002222', 'Vault 2', 'This is a testvault.', '2020-02-20 20:20:20', 'salt2', 42, 'masterkey2',
	 'MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEC1uWSXj2czCDwMTLWV5BFmwxdM6PX9p+Pk9Yf9rIf374m5XP1U8q79dBhLSIuaojsvOT39UUcPJROSD1FqYLued0rXiooIii1D3jaW6pmGVJFhodzC31cy5sfOYotrzF',
	 'MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDCAHpFQ62QnGCEvYh/pE9QmR1C9aLcDItRbslbmhen/h1tt8AyMhskeenT+rAyyPhGhZANiAAQLW5ZJePZzMIPAxMtZXkEWbDF0zo9f2n4+T1h/2sh/fviblc/VTyrv10GEtIi5qiOy85Pf1RRw8lE5IPUWpgu553SteKigiKLUPeNpbqmYZUkWGh3MLfVzLmx85ii2vMU=',
	 FALSE),
	('7E57C0DE-0000-4000-8000-0001AAAAAAAA', 'Vault Archived', 'This is a archived vault.', '2020-02-20 20:20:20', 'salt3', 42, 'masterkey3',
	 'MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEC1uWSXj2czCDwMTLWV5BFmwxdM6PX9p+Pk9Yf9rIf374m5XP1U8q79dBhLSIuaojsvOT39UUcPJROSD1FqYLued0rXiooIii1D3jaW6pmGVJFhodzC31cy5sfOYotrzF',
	 'MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDCAHpFQ62QnGCEvYh/pE9QmR1C9aLcDItRbslbmhen/h1tt8AyMhskeenT+rAyyPhGhZANiAAQLW5ZJePZzMIPAxMtZXkEWbDF0zo9f2n4+T1h/2sh/fviblc/VTyrv10GEtIi5qiOy85Pf1RRw8lE5IPUWpgu553SteKigiKLUPeNpbqmYZUkWGh3MLfVzLmx85ii2vMU=',
	 TRUE);

INSERT INTO "vault_access" ("vault_id", "authority_id")
VALUES
	('7E57C0DE-0000-4000-8000-000100001111', 'user1'),
	('7E57C0DE-0000-4000-8000-000100001111', 'user2'),
	('7E57C0DE-0000-4000-8000-0001AAAAAAAA', 'user1'),
	('7E57C0DE-0000-4000-8000-000100002222', 'group1'); /* user1, part of group1, has access to vault2 */

INSERT INTO "device" ("id", "owner_id", "name", "type", "publickey", "creation_time")
VALUES
	('device1', 'user1', 'Computer 1', 'DESKTOP', 'publickey1', '2020-02-20 20:20:20'),
	('device2', 'user2', 'Computer 2', 'DESKTOP', 'publickey2', '2020-02-20 20:20:20'),
	('device3', 'user1', 'Computer 3', 'DESKTOP', 'publickey3', '2020-02-20 20:20:20'); /* user1 is part of group1 */

INSERT INTO "access_token" ("device_id", "vault_id", "jwe")
VALUES
	('device1', '7E57C0DE-0000-4000-8000-000100001111', 'jwe1'),
	('device2', '7E57C0DE-0000-4000-8000-000100001111', 'jwe2'),
	('device3', '7E57C0DE-0000-4000-8000-000100002222', 'jwe3'); -- device3 of user1, part of group1, has access to vault2

INSERT INTO "audit_event" ("id", "timestamp", "type")
VALUES
    (999, '1970-01-01T00:00:02.900Z', 'CREATE_VAULT'),
    (1000, '1970-01-01T00:00:03Z', 'UPDATE_VAULT_MEMBERSHIP'),
    (1001, '1970-01-01T00:00:04Z', 'UPDATE_VAULT_MEMBERSHIP'),
    (1111, '1970-01-01T00:00:04.999Z', 'UNLOCK_VAULT'),
    (4242, '1970-01-01T00:00:05Z', 'UNLOCK_VAULT');

INSERT INTO "unlock_vault_event" ("id", "user_id", "vault_id", "device_id", "result")
VALUES
    (1111, 'user1', '7E57C0DE-0000-4000-8000-000100001111', 'device3', 'UNAUTHORIZED'),
    (4242, 'user1', '7E57C0DE-0000-4000-8000-000100001111', 'device1', 'SUCCESS');

INSERT INTO "create_vault_event" ("id", "user_id", "vault_id")
VALUES
    (999, 'user1', '7E57C0DE-0000-4000-8000-000100001111');

INSERT INTO "update_vault_membership_event" ("id", "user_id", "vault_id", "authority_id", "operation")
VALUES
    (1000, 'user1', '7E57C0DE-0000-4000-8000-000100001111', 'user2', 'ADD'),
    (1001, 'user1', '7E57C0DE-0000-4000-8000-000100001111', 'user2', 'REMOVE');
