# JavaBot — AI Chatbot (Java)

A rule-based chatbot with basic NLP techniques, a console interface, and an
optional Swing GUI for real-time chat. Pure Java, **no external libraries** —
compiles with just the JDK.

## Files

| File               | Purpose                                                             |
|---------------------|----------------------------------------------------------------------|
| `NLPUtils.java`     | Basic NLP: normalization, tokenizing, stop-word removal, a lightweight stemmer, synonym/slang mapping, Levenshtein distance for fuzzy matching |
| `Intent.java`       | Represents one topic the bot understands (patterns + responses) and scores itself against user input |
| `KnowledgeBase.java`| The bot's "training data" — built-in intents plus persisted, user-taught Q&A pairs |
| `ChatEngine.java`   | The core logic: routes input through teach-commands, name/entity extraction, math evaluation, time/date, then rule-based intent matching |
| `Main.java`         | Console chat interface |
| `ChatBotGUI.java`   | Swing GUI chat window (optional interface) |

## How to compile and run

You need a JDK (Java 8+) installed.

```bash
# Compile everything
javac *.java

# Run the console version
java Main

# Run the graphical (Swing) version
java ChatBotGUI
```

## Features

- **Rule-based intent matching** — ~18 built-in intents (greetings, thanks,
  farewells, bot identity/creator, small talk, mood check-ins, jokes,
  compliments/insults, FAQ answers like "what is AI/Java/a chatbot", etc.)
- **Basic NLP pipeline** — text normalization → tokenization → stop-word
  removal → synonym/slang normalization ("hey"/"yo" → "hello") → a small
  suffix-stripping stemmer, then a precision×recall keyword-overlap score
  (with Levenshtein-based fuzzy matching so small typos still match).
- **Entity extraction** — recognizes the user's name from phrases like
  *"my name is Alex"* / *"I'm Alex"* / *"call me Alex"* and personalizes
  later greetings.
- **Basic arithmetic** — understands both symbols and words, e.g.
  `"what is 12 * 7"`, `"10 divided by 2"`, `"5 plus 3"`.
- **Live time/date answers** — computed at request time, not canned text.
- **Runtime training** — teach the bot new answers without recompiling:
  ```
  teach: what is your favorite language => I love Java, obviously!
  ```
  Taught pairs are saved to `chatbot_data/trained_qa.txt` and reloaded
  automatically on the next run, so the bot's knowledge grows over time.
- **Console + GUI** — same `ChatEngine` powers both interfaces, so anything
  you teach or fix in `KnowledgeBase.java` improves both automatically.

## Extending the bot

To add more built-in knowledge, open `KnowledgeBase.java` and add another
call to `addIntent(...)` inside `loadDefaultIntents()`:

```java
addIntent("your_tag",
    new String[]{"pattern one", "pattern two", "another way to ask it"},
    new String[]{"Response option A", "Response option B"});
```

The bot will pick a random response from the list each time that intent
matches, and will keep matching paraphrases of your patterns thanks to the
keyword-overlap + fuzzy-matching logic in `Intent.java`.

## Notes

- `chatbot_data/trained_qa.txt` is created automatically the first time you
  use the `teach:` command — delete it to reset the bot's learned knowledge
  back to just the built-in intents.
- The match confidence threshold (how confident the bot must be before
  answering, vs. falling back to "I'm not sure I understand") is set in
  `KnowledgeBase.MATCH_THRESHOLD` — lower it for looser matching, raise it
  for stricter matching.
