package pl.qprogramming.shopper.watch.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Device {
    private String id;
    private String plainKey;
    private String email;
    private String deviceKey;
    private boolean enabled;
    private String name;
    private Date lastUsed;
}
