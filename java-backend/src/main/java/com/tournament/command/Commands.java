package com.tournament.command;

// ═══════════════════════════════════════════════════════════════════
//  COMMAND PATTERN
//  ────────────────
//  Command      — interface with execute()
//  ConcreteCmd  — RegisterTeamCommand, ScheduleMatchCommand,
//                 UpdateScoreCommand, MakePaymentCommand
//  Invoker      — SystemController  (queues & fires commands)
//  Receiver     — TournamentSystem  (actual business logic)
// ═══════════════════════════════════════════════════════════════════

import java.time.LocalDateTime;
import java.util.*;

// ── Command Interface ──────────────────────────────────────────────
interface Command {
    void execute();
    String getCommandName();
}

// ── Receiver: TournamentSystem ─────────────────────────────────────
class TournamentSystem {

    public void registerTeam(int teamId, int tournamentId) {
        System.out.printf("[TournamentSystem] Team #%d registered for tournament #%d%n",
                teamId, tournamentId);
        // In real implementation: persist to DB via JDBC
    }

    public void scheduleMatch(int tournamentId, int team1Id, int team2Id,
                               int refereeId, LocalDateTime matchDate) {
        System.out.printf("[TournamentSystem] Match scheduled: T%d vs T%d | Referee #%d | %s%n",
                team1Id, team2Id, refereeId, matchDate);
    }

    public void updateScore(int matchId, String team1Score, String team2Score, int winnerTeamId) {
        System.out.printf("[TournamentSystem] Score updated for Match #%d: %s – %s | Winner team #%d%n",
                matchId, team1Score, team2Score, winnerTeamId);
    }

    public void makePayment(int registrationId, double amount) {
        // Simulates calling external Payment Gateway
        String gatewayRef = "GW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.printf("[TournamentSystem] Payment processed: regId=%d, amount=%.2f, ref=%s%n",
                registrationId, amount, gatewayRef);
    }
}

// ── Concrete Command 1: RegisterTeamCommand ────────────────────────
class RegisterTeamCommand implements Command {
    private final TournamentSystem receiver;
    private final int teamId;
    private final int tournamentId;

    public RegisterTeamCommand(TournamentSystem receiver, int teamId, int tournamentId) {
        this.receiver     = receiver;
        this.teamId       = teamId;
        this.tournamentId = tournamentId;
    }

    @Override
    public void execute() {
        receiver.registerTeam(teamId, tournamentId);
    }

    @Override
    public String getCommandName() {
        return "RegisterTeamCommand(team=" + teamId + ", tournament=" + tournamentId + ")";
    }
}

// ── Concrete Command 2: ScheduleMatchCommand ───────────────────────
class ScheduleMatchCommand implements Command {
    private final TournamentSystem receiver;
    private final int tournamentId;
    private final int team1Id;
    private final int team2Id;
    private final int refereeId;
    private final LocalDateTime matchDate;

    public ScheduleMatchCommand(TournamentSystem receiver, int tournamentId,
                                 int team1Id, int team2Id,
                                 int refereeId, LocalDateTime matchDate) {
        this.receiver     = receiver;
        this.tournamentId = tournamentId;
        this.team1Id      = team1Id;
        this.team2Id      = team2Id;
        this.refereeId    = refereeId;
        this.matchDate    = matchDate;
    }

    @Override
    public void execute() {
        receiver.scheduleMatch(tournamentId, team1Id, team2Id, refereeId, matchDate);
    }

    @Override
    public String getCommandName() {
        return "ScheduleMatchCommand(t=" + tournamentId + ", " + team1Id + " vs " + team2Id + ")";
    }
}

// ── Concrete Command 3: UpdateScoreCommand ─────────────────────────
class UpdateScoreCommand implements Command {
    private final TournamentSystem receiver;
    private final int    matchId;
    private final String team1Score;
    private final String team2Score;
    private final int    winnerTeamId;

    public UpdateScoreCommand(TournamentSystem receiver, int matchId,
                               String team1Score, String team2Score, int winnerTeamId) {
        this.receiver     = receiver;
        this.matchId      = matchId;
        this.team1Score   = team1Score;
        this.team2Score   = team2Score;
        this.winnerTeamId = winnerTeamId;
    }

    @Override
    public void execute() {
        receiver.updateScore(matchId, team1Score, team2Score, winnerTeamId);
    }

    @Override
    public String getCommandName() {
        return "UpdateScoreCommand(match=" + matchId + ", " + team1Score + " vs " + team2Score + ")";
    }
}

// ── Concrete Command 4: MakePaymentCommand ─────────────────────────
class MakePaymentCommand implements Command {
    private final TournamentSystem receiver;
    private final int    registrationId;
    private final double amount;

    public MakePaymentCommand(TournamentSystem receiver, int registrationId, double amount) {
        this.receiver       = receiver;
        this.registrationId = registrationId;
        this.amount         = amount;
    }

    @Override
    public void execute() {
        receiver.makePayment(registrationId, amount);
    }

    @Override
    public String getCommandName() {
        return "MakePaymentCommand(reg=" + registrationId + ", amount=" + amount + ")";
    }
}

// ── Invoker: SystemController ──────────────────────────────────────
/**
 * Queues commands and executes them in order.
 * Decouples the caller from the receiver entirely.
 */
class SystemController {
    private final Queue<Command> commandQueue = new LinkedList<>();
    private final List<String>   history      = new ArrayList<>();

    /** Add a command to the execution queue. */
    public void addCommand(Command command) {
        commandQueue.offer(command);
        System.out.println("[SystemController] Queued: " + command.getCommandName());
    }

    /** Execute all queued commands in FIFO order. */
    public void executeAll() {
        while (!commandQueue.isEmpty()) {
            Command cmd = commandQueue.poll();
            System.out.println("[SystemController] Executing: " + cmd.getCommandName());
            cmd.execute();
            history.add(cmd.getCommandName());
        }
    }

    /** Execute a single command immediately (bypass queue). */
    public void executeNow(Command command) {
        System.out.println("[SystemController] Executing immediately: " + command.getCommandName());
        command.execute();
        history.add(command.getCommandName());
    }

    public List<String> getHistory() { return Collections.unmodifiableList(history); }
    public int          getPending()  { return commandQueue.size(); }
}
