package com.scottmo.util;

import com.healthmarketscience.common.util.AppendableExt;
import com.healthmarketscience.sqlbuilder.Expression;
import com.healthmarketscience.sqlbuilder.SqlObject;
import com.healthmarketscience.sqlbuilder.ValidationContext;

import java.io.IOException;

public class SqlUtils {
    public static final SqlObject AUTO_INCREMENT = new Expression() {
        public void appendTo(AppendableExt app) throws IOException {
            app.append("AUTOINCREMENT");
        }
        protected void collectSchemaObjects(ValidationContext vContext) {}
    };
}
