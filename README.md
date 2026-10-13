# Kaftar Messenger

Experience the next generation of secure, ultra-fast, and futuristic communication featuring a cyber-neon aesthetic interface powered by a robust Java backend and SQLite database.

---

## 🚀 Key Features

* **Authentication & Security**
* Supports user login, account registration, and password recovery workflows.


* Implements secure password hashing utilizing SHA-256.


* Includes brute-force protection with account locking after 5 failed login attempts.




* **Chat Management**
* **Private Chats (PV):** One-on-one direct messaging with read/unread tracking and history management.


* **Saved Messages:** Dedicated personal space for storing notes and media.


* **Group Chats:** Create groups, assign owners, add or remove members, update group names, and manage group avatars.




* **Interactive Messaging**
* Real-time polling and message updates.


* Support for sending text messages, base64 images, and custom sticker packs.


* Message editing, message deletion, and message reporting functionalities.


* Group history tracking for edited and deleted messages.




* **Advanced Chat Controls**
* Pin and unpin chat rooms.


* Archive and unarchive chats into dedicated folders.


* User contact management (add/remove contacts).


* User blocking and unblocking capabilities.




* **Comprehensive Admin Control Panel**
* Web-based admin panel (`admin.html`) for managing users, groups, group memberships, and reported messages.


* Alternative CLI admin interface (`Admin.java`) for server-side management.





---

## 🛠️ Architecture & Tech Stack

* **Backend:** Built with Java using the built-in `HttpServer` running on port 8081, handling RESTful API endpoints and CORS configuration.


* **Database:** SQLite relational database (`data/messenger.db`) managed via `DatabaseManager` and dedicated data repositories (`UserRepository`, `AllGroupsRepository`, `InformationRepository`, `ReportRepository`, etc.).


* **Frontend:** Responsive vanilla HTML5, CSS3, and modular JavaScript files (`app.js`, `chatrooms.js`, `messages.js`, `modals.js`, `admin.js`, etc.) utilizing FontAwesome icons.



---

## 📡 API Endpoints Overview

| Endpoint | Method | Description | Source |
| --- | --- | --- | --- |
| `/api/login` | POST | Handles user authentication and account registration |  |
| `/api/user/recover` | POST | Recovers and updates user passwords |  |
| `/api/user/delete` | POST | Deletes user accounts, owned groups, and associated data |  |
| `/api/user/init` | GET | Initializes user session data, profile details, and active chat lists |  |
| `/api/user/info` | GET | Retrieves user status and last seen timestamps |  |
| `/api/user/search` | GET | Searches for users by username |  |
| `/api/user/avatar` | GET / POST | Loads or updates user profile pictures (Base64) |  |
| `/api/details` | GET | Fetches detailed metadata for specific users or groups |  |
| `/api/user/update` | POST | Updates user full name and biography |  |
| `/api/group` | POST / GET / DELETE | Creates groups, manages membership, loads messages, or deletes groups |  |
| `/api/group/update` | POST | Updates group names |  |
| `/api/group/avatar` | GET / POST | Loads or updates group avatars |  |
| `/api/group/deleted` | GET | Retrieves group logs of deleted messages |  |
| `/api/group/edited` | GET | Retrieves group logs of edited messages |  |
| `/api/pv` | POST / GET / DELETE | Sends, loads, searches, or deletes private chat messages |  |
| `/api/chatroom/state` | POST | Updates chat states (pinned, archived, normal) and unread counts |  |
| `/api/contacts` | GET / POST | Manages user contact lists |  |
| `/api/block` | GET / POST | Checks, blocks, or unblocks users |  |
| `/api/message` | PUT / DELETE / POST | Edits, deletes, or reports specific messages |  |
| `/api/admin/login` | POST | Authenticates administrators |  |
| `/api/admin/users` | GET | Lists all registered users for administration |  |
| `/api/admin/groups` | GET | Lists all groups and their members |  |
| `/api/admin/reports` | GET / DELETE | Views or clears reported messages |  |

---

## 🗄️ Database Schema (`messenger.db`)

The SQLite database initializes with the following relational tables:

* **`users`**: Stores user ID, username, full name, hashed password, avatar, bio, and last seen timestamp.


* **`groups`**: Stores group ID, name, owner ID, and avatar.


* **`group_members`**: Maps group IDs to user IDs for membership tracking.


* **`messages`**: Records message identifiers, sender ID, receiver ID, type (`is_group`), text content, timestamp, edited status, seen status, and deletion status.


* **`contacts`**: Links user IDs to their respective saved contact IDs.


* **`blocked_users`**: Tracks blocked relationships between users.


* **`chat_states`**: Maintains custom states (`pinned`, `archived`, `normal`) and unread message counters per user per chat.


* **`reports`**: Stores message reports submitted by users for administrative review.



---

## 🛠️ Getting Started

1. **Prerequisites:** Ensure you have the Java Development Kit (JDK) installed alongside an SQLite-compatible environment.
2. **Run the Server:** Compile and execute `Server.java`. The HTTP server will start on port `8081`, and the SQLite database (`data/messenger.db`) will be automatically initialized.


3. **Launch the Client:** Open `index.html` in any modern web browser or serve the frontend files via a local static server to access the Kaftar Messenger client interface.


4. **Access Admin Panel:** Navigate to `admin.html` and log in with the administrator credentials (`poori` / `popopo`) to manage the system.
