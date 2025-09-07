# Forest Fires Analytics (Java + SQLite)

### Prerequisites
- Java 17+
- Maven 3.8+

### Run
```bash
mvn -q -DskipTests exec:java | cat
```
- First run creates `forest.db`, runs migrations, and imports `datafile.csv`.
- Use the menu to insert/update and run analytics.

### Features
- Insert/Update per state & year (upsert)
- Top 5 states by year
- YoY growth/decline per state
- Compare multiple states (time series)
- Summary table for a chosen year
- States with 0 fires
- Most vulnerable region overall

### Notes
- Year labels like `2010-2011`/`2009-10` parsed as the starting year.
- DB file: `forest.db` in project root.
```
States/UTs,2010-2011,2009-10,2008-09
...
```
