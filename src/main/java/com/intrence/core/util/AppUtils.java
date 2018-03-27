/**
 * Created by wliu on 12/21/17.
 */
package com.intrence.core.util;

import javax.ws.rs.BadRequestException;
import java.util.UUID;

public class AppUtils {

    public static UUID validateAndReturnUUID(String uuidString) throws Exception {
        try {
            UUID uuid = UUID.fromString(uuidString);
            return uuid;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(String.format("%s is not a valid UUID.", uuidString));
        }
    }
}
