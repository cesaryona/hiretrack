package br.com.hiretrack.application.domain;

public enum ApplicationStatus {
    RECEIVED,
    UNDER_REVIEW,
    INTERVIEW,
    APPROVED,
    REJECTED;

    public java.util.List<ApplicationStatus> availableTransitions() {
        return java.util.Arrays.stream(values()).filter(this::canTransitionTo).toList();
    }

    public boolean canTransitionTo(ApplicationStatus target) {
        return switch (this) {
            case RECEIVED -> target == UNDER_REVIEW;
            case UNDER_REVIEW -> target == INTERVIEW || target == REJECTED;
            case INTERVIEW -> target == APPROVED || target == REJECTED;
            case APPROVED, REJECTED -> false;
        };
    }
}
