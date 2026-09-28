package br.com.hiretrack.notification;

import br.com.hiretrack.application.ApplicationStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
class ApplicationStatusNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusNotificationListener.class);

    @ApplicationModuleListener
    void on(ApplicationStatusChangedEvent event) {
        log.info("Notifying candidate {} <{}> — application {} moved from {} to {}",
                event.candidateName(), event.candidateEmail(),
                event.applicationId(), event.previousStatus(), event.newStatus());
    }
}
