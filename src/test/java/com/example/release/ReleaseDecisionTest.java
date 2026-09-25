package com.example.release;

public final class ReleaseDecisionTest {
    public static void main(String[] args) {
        ReleaseDecision enabled = ReleaseDecision.evaluate(true, true);
        if (!enabled.proceed() || !enabled.action().equals("apply-schema-and-release")) throw new AssertionError("enabled path");
        ReleaseDecision disabled = ReleaseDecision.evaluate(false, true);
        if (disabled.proceed() || !disabled.action().equals("rollback-to-previous-schema")) throw new AssertionError("rollback path");
        System.out.println("ReleaseDecisionTest passed");
    }
}
