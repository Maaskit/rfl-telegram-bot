package com.rflbot.rflprojectbot.CallbackHandler;

import com.rflbot.commands.ClaimCommand;
import com.rflbot.commands.Command;
import com.rflbot.commands.DisableCommand;
import com.rflbot.commands.InfoCommand;
import com.rflbot.commands.ShowStreakCommand;
import com.rflbot.commands.StartMenuCommand;
import com.rflbot.commands.TimeSelectionCommand;
import com.rflbot.handler.CallbackHandler;
import com.rflbot.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackHandlerTest {

    @Mock
    private MessageService messageService;
    @Mock
    private StartMenuCommand startMenuCommand;
    @Mock
    private ClaimCommand claimCommand;
    @Mock
    private InfoCommand infoCommand;
    @Mock
    private TimeSelectionCommand timeSelectionCommand;
    @Mock
    private DisableCommand disableCommand;
    @Mock
    private ShowStreakCommand showStreakCommand;
    @Mock
    private Update update;
    @Mock
    private CallbackQuery callbackQuery;

    private CallbackHandler callbackHandler;

    @BeforeEach
    void setUp() {
        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);

        List<Command> mockCommands = List.of(
                startMenuCommand,
                claimCommand,
                infoCommand,
                timeSelectionCommand,
                disableCommand,
                showStreakCommand
        );

        callbackHandler = new CallbackHandler(mockCommands, messageService);
    }

    @Test
    void shouldCallStartMenuCommand() {

        when(startMenuCommand.canHandle(update)).thenReturn(true);

        handleAndVerifyDelete();

        verify(startMenuCommand).handle(update);

        verify(claimCommand, never()).handle(any());
        verify(infoCommand, never()).handle(any());
        verify(timeSelectionCommand, never()).handle(any());
        verify(disableCommand, never()).handle(any());
        verify(showStreakCommand, never()).handle(any());
    }

    @Test
    void shouldCallClaimCommand() {
        when(claimCommand.canHandle(update)).thenReturn(true);

        handleAndVerifyDelete();

        verify(claimCommand).handle(update);

        verify(startMenuCommand, never()).handle(any());
        verify(infoCommand, never()).handle(any());
        verify(timeSelectionCommand, never()).handle(any());
        verify(disableCommand, never()).handle(any());
        verify(showStreakCommand, never()).handle(any());
    }

    @Test
    void shouldCallInfoCommand() {
        when(infoCommand.canHandle(update)).thenReturn(true);

        handleAndVerifyDelete();

        verify(infoCommand).handle(update);

        verify(startMenuCommand, never()).handle(any());
        verify(claimCommand, never()).handle(any());
        verify(timeSelectionCommand, never()).handle(any());
        verify(disableCommand, never()).handle(any());
        verify(showStreakCommand, never()).handle(any());
    }

    @Test
    void shouldCallTimeSelectionCommand() {
        when(timeSelectionCommand.canHandle(update)).thenReturn(true);

        handleAndVerifyDelete();

        verify(timeSelectionCommand).handle(update);

        verify(startMenuCommand, never()).handle(any());
        verify(claimCommand, never()).handle(any());
        verify(infoCommand, never()).handle(any());
        verify(disableCommand, never()).handle(any());
        verify(showStreakCommand, never()).handle(any());
    }

    @Test
    void shouldNotCallAnyCommandForUnknownCallback() {
        handleAndVerifyDelete();

        verify(startMenuCommand, never()).handle(any());
        verify(claimCommand, never()).handle(any());
        verify(infoCommand, never()).handle(any());
        verify(disableCommand, never()).handle(any());
        verify(showStreakCommand, never()).handle(any());
        verify(timeSelectionCommand, never()).handle(any());
    }

    @Test
    void shouldAlwaysDeleteCallbackMessage() {
        when(startMenuCommand.canHandle(update)).thenReturn(true);

        handleAndVerifyDelete();
    }

    private void handleAndVerifyDelete() {
        callbackHandler.handle(update);
        verify(messageService).deleteCallbackMessage(callbackQuery);
    }
}