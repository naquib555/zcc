package com.zendesk.zcc.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class TicketList {

    private String paginationInfo;
    private Integer currentPage;
    private Integer previousPage;
    private Integer nextPage;
    private Integer totalTickets;
    private Integer pageTicketCount;
    public List<Ticket> tickets;
}
