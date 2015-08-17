# Changelog

## v0.2.0
- [x] Lexer.name and Lexer.text are now private
- [x] Lexer.state is no longer protected, but private
- [x] Lexer.setState is no longer protected, but private
- [x] Lexer's constructors are not protected
- [x] Removed deprecated whitespace methods
- [x] Add Tokenizer interface
- [x] Pulled expect out into a seperate class, now usable for Tokenizer

## v0.1.4

- [x] Make Token constructor public.
- [x] Deprecated whitespace().
- [x] Added space() and spaceNoLine().
- [x] Added isolated tests for each Lexer function.