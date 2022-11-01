package com.scottmo.services.ppt;

import java.util.List;

public record InsertAction(
        String templatePath,
        String dataPath,
        String insertIndex,
        List<InsertParameters> parameters
) {
}

record InsertParameters(
        int templateIndex,
        String scope
) {}
