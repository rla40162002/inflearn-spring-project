package com.studylecture.zone;

import com.studylecture.domain.Zone;
import lombok.Data;

@Data
public class ZoneForm {

    private String zoneName;

    public String getCityName() {
        if (zoneName.contains("(")) {
            return zoneName.substring(0, zoneName.indexOf("("));
        }
        return "none";
    }

    public String getProvinceName() {
        if (zoneName.contains("/")) {
            return zoneName.substring(zoneName.indexOf("/") + 1);
        }
        return "none";
    }

    public String getLocalNameOfCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public Zone getZone() {
        return Zone.builder().city(this.getCityName())
                .localNameOfCity(this.getLocalNameOfCity())
                .province(this.getProvinceName()).build();
    }

}
