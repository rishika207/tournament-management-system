package com.tournament.model;

/**
 * Base class for all system actors.
 * Demonstrates OOP Inheritance — Admin, Organizer, TeamManager, Referee, Viewer all extend this.
 */
public abstract class User {
    protected int    userId;
    protected String name;
    protected String email;
    protected String role;

    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name   = name;
        this.email  = email;
        this.role   = role;
    }

    // ── Lifecycle ──────────────────────────────────────
    public void login()  { System.out.println(name + " [" + role + "] logged in.");  }
    public void logout() { System.out.println(name + " [" + role + "] logged out."); }

    // ── Abstract: each actor has a distinct dashboard ──
    public abstract void showDashboard();

    // ── Getters ────────────────────────────────────────
    public int    getUserId() { return userId; }
    public String getName()   { return name;   }
    public String getEmail()  { return email;  }
    public String getRole()   { return role;   }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s', role='%s'}",
                userId, name, email, role);
    }
}
