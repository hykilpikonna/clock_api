# clock_api

## Node

**GET** `/register`

|    Name    |   Type   |    In    |                         Description                         |
| :--------: | :------: | :------: | :---------------------------------------------------------: |
| `username` | `string` | `header` | User's name, should match the regex `/^[a-z0-9_-]{3,16}$/`. |
| `password` | `string` | `header` |              Only password's md5 will be save.              |

**Response**

| Scenario |     Http Status      |     Type     |          Value           |
| :------: | :------------------: | :----------: | :----------------------: |
| Success  |       `200 OK`       |   `string`   |       User's uuid        |
| Failure  | `406 NOT ACCEPTABLE` | `json array` | JSON Array of Error Code |

## Error Code

|  Code   |           Description           |
| :-----: | :-----------------------------: |
| `A0101` |       `username` is null.       |
| `A0102` |       `password` is null        |
| `A0111` | `username` not match the regex. |
| `A0112` | `password` not match the regex. |

