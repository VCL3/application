package com.intrence.core.persistence.dao;

import com.intrence.core.authentication.Token;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

public interface TokenDao {

    @SqlQuery()
    Token getToken();
}
