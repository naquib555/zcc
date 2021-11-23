package com.zendesk.zcc.controller;

import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.domain.ViewModel;
import com.zendesk.zcc.service.IBaseService;
import com.zendesk.zcc.service.impl.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("ticket")
public class TicketController {

    private IBaseService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("")
    public String homePage() {

        return "home";
    }

    @GetMapping("info/{id}")
    public String ticketInformationPage(@PathVariable String id, Model model) {

        ViewModel<Ticket> ticketViewModel = ticketService.getObject(id);
        model.addAttribute("ticket", ticketViewModel);
        return "ticket";
    }

    @GetMapping("showAll")
    public String allTicketsPage(@RequestParam(required = false) Integer page, Model model) {

        ViewModel<TicketList> ticketListViewModel = ticketService.getAll(page);
        model.addAttribute("allTickets", ticketListViewModel);
        return "allTickets";
    }

}
