package com.qprogramming.shopper.app.account.avatar;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

/**
 * Created by Khobar on 06.03.2017.
 */
@Getter
@Setter
@Entity
public class Avatar {

    @Id
    private String id;
    @Lob
    private byte[] image;
    @Column
    private Date created;
    @Column
    private String type;
    public Avatar() {
        created = new Date();
    }
}
