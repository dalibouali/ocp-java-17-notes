# OCP Java SE 17 (1Z0-829) — Code Notebook

This repository is my hands-on study notebook for the Oracle Certified Professional Java SE 17 Developer exam (1Z0-829).

## How I study
For each topic:
- I write small runnable `.java` files
- I include both compiling and intentionally non-compiling examples
- I keep a `Notes.md` per folder with rules + exam traps

## Rules (exam mindset)
- Ask “Does it compile?” before “What does it print?”
- For every concept: write 1 valid + 1 invalid + 1 tricky example
- Prefer minimal code that exposes one rule at a time

## Folder map
01-lambdas — functional interfaces, method refs, overload ambiguity, var rules  
02-streams — stream pipelines, collectors, optional, parallel traps  
03-generics — bounds, wildcards, inference, legacy/raw types  
04-concurrency — executors, futures, sync, concurrent collections  
05-nio2 — Path/Files API, walk/find, normalize/resolve, IO pitfalls  
06-exceptions — try-with-resources, suppressed, multi-catch, rethrow  
07-datetime — java.time, zones, parsing/formatting, periods/durations  
08-localization — Locale, ResourceBundle, formatting  
09-jdbc — basics, ResultSet, transactions, prepared statements  
10-collections-comparators — sorting/searching, map/set behavior  
11-modules — module-info.java, requires/exports, services basics  
12-annotations — meta-annotations, retention/target, repeatable  
14-java17-features — records, sealed, pattern matching, switch, text blocks  
exam-traps — compilation vs runtime + common gotchas for final review
