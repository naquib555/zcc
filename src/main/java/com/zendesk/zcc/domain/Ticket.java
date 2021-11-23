package com.zendesk.zcc.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor @Getter @ToString
public class Ticket {

    private String id;
    private String subject;
    private String description;
    private String status;
}
