# Tusks Mocha Garden

A cafe management system built with Java, JavaFX, and MySQL — covering staff accounts, inventory, a point-of-sale register, order tracking, and a sales dashboard.

## Features

- **Authentication** — login, registration, and security-question password recovery, with bcrypt-hashed credentials.
- **Dashboard** — daily sales stats, day-over-day trends, low-stock alerts, top sellers, and 7-day income/order charts.
- **Inventory management** — product CRUD, stock tracking, image upload, category filters.
- **Register (POS)** — visual menu cards, drink customization, cart management, cash/card/wallet payment, receipt printing.
- **Order tracking** — active/completed order queues with per-order status advancement (Prep → Ready → Served).
- **Staff management** — add/edit/remove accounts and roles (Admin/Barista).

## Architecture

The codebase is layered under `com.tusksmochagarden`:

- `app` — application bootstrap (`TusksMochaGardenApplication`) and startup schema migration (`SchemaUpdater`).
- `controller` — JavaFX FXML controllers; UI state and event handling only.
- `model` — plain data classes (`Product`, `CustomerTransaction`, `AppSession`).
- `data` — persistence: `Database` (connection pooling), `PasswordHasher`, and repositories (`ProductRepository`, `OrderRepository`, `StaffRepository`, `DashboardRepository`) that own all SQL for their domain.

Controllers hold no JDBC — they call into repositories, which each return plain data (lists, records, or model objects) for the controller to render.

## Requirements

- Java 17+
- MySQL 8.0+
- Gradle (wrapper included, no local install needed)

## Setup

1. **Database**: create a MySQL database named `tusks_mocha_garden`. The app creates/migrates its own tables on startup (see `SchemaUpdater` and `database_updates.sql` for reference).

2. **Credentials**: the app reads connection settings from environment variables — nothing is hardcoded:

   | Variable | Default | Required |
   |---|---|---|
   | `DB_URL` | `jdbc:mysql://localhost:3306/tusks_mocha_garden` | no |
   | `DB_USERNAME` | `root` | no |
   | `DB_PASSWORD` | — | **yes** |

   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/tusks_mocha_garden"
   export DB_USERNAME="root"
   export DB_PASSWORD="your-password-here"
   ```

3. **Run**:

   ```bash
   ./gradlew run
   ```

4. **Test**:

   ```bash
   ./gradlew test
   ```

   `LoginTest` and `DatabaseTest` exercise real queries against a live MySQL instance configured via the environment variables above.

## Project structure

- `src/main/java/com/tusksmochagarden/` — application source (`app`, `controller`, `model`, `data`)
- `src/main/resources/com/tusksmochagarden/` — FXML views, CSS, fonts
- `src/test/java/com/tusksmochagarden/` — JUnit 5 tests
- `lib/` — MySQL connector JAR

## Known limitations

- Receipt generation writes plain-text files to a local `receipts/` directory rather than producing PDFs or sending to a physical printer.

## License

MIT — see [LICENSE](LICENSE).
