package io.github.sinri.keel.facade.tesuto;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

public interface KeelJUnit5TestCore {
    KeelIssueRecorder<KeelEventLog> getUnitTestLogger();
}
