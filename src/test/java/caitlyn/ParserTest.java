package caitlyn;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

/** Tests that complete user commands are dispatched to the correct command type. */
class ParserTest {
    @Test
    void parse_recognizesCommandsAndTrimsSurroundingWhitespace() {
        assertInstanceOf(ExitCommand.class, Parser.parse("  bye  "));
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
        assertInstanceOf(FindCommand.class, Parser.parse("find book"));
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 1"));
        assertInstanceOf(MarkCommand.class, Parser.parse("unmark 1"));
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 1"));
        assertInstanceOf(TodoCommand.class, Parser.parse("todo buy milk"));
        assertInstanceOf(DeadlineCommand.class, Parser.parse("deadline return book /by 2025-03-14"));
        assertInstanceOf(EventCommand.class,
                Parser.parse("event meeting /from 2025-03-14 /to 2025-03-15"));
    }

    @Test
    void parse_returnsUnknownCommandForUnrecognizedOrIncompleteInput() {
        assertInstanceOf(UnknownCommand.class, Parser.parse("dance"));
        assertInstanceOf(UnknownCommand.class, Parser.parse(""));
        assertInstanceOf(UnknownCommand.class, Parser.parse("todoish something"));
        assertInstanceOf(UnknownCommand.class, Parser.parse("deadlineish something"));
    }
}
