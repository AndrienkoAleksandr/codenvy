/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.metrics.tasks.builds;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.tasks.TasksStoppedNormally;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Anatoliy Bazko */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class BuildsFinishedNormally extends TasksStoppedNormally {

    public BuildsFinishedNormally() {
        super(MetricType.BUILDS_FINISHED_NORMALLY);
    }

    @Override public Context applySpecificFilter(Context context) throws IOException {
        Context.Builder builder = new Context.Builder(super.applySpecificFilter(context));
        builder.put(MetricFilter.TASK_TYPE, BUILDER);
        builder.put(MetricFilter.SHUTDOWN_TYPE, NormalShutdownType.NORMAL.name().toLowerCase());
        return builder.build();
    }

    @Override
    public String getDescription() {
        return "The number of builds stopped normally";
    }
}