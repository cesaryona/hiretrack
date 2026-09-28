package br.com.hiretrack.application.infra;

import br.com.hiretrack.application.ApplicationStatusChangedEvent;
import br.com.hiretrack.application.domain.ApplicationStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ApplicationStatusHistoryListener {

    private final ApplicationStatusHistoryRepository repository;

    @ApplicationModuleListener
    void on(ApplicationStatusChangedEvent event) {
        repository.save(new ApplicationStatusHistory(event.applicationId(), event.previousStatus(), event.newStatus()));
    }
}
