package com.zendesk.zcc.controller;

import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.domain.ViewModel;
import com.zendesk.zcc.service.ZendeskApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("ticket")
public class TicketController {

    private ZendeskApiService zendeskApiService;

    public TicketController(ZendeskApiService zendeskApiService) {
        this.zendeskApiService = zendeskApiService;
    }

    @GetMapping("")
    public String homePage() {

        return "home";
    }

    @GetMapping("info/{id}")
    public String ticketInformationPage(@PathVariable String id, Model model) {

        ViewModel<Ticket> viewModel = new ViewModel<>();

        try {
            Ticket ticket = zendeskApiService.getTicket(id);
            viewModel.setStatus(true);
            viewModel.setData(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            viewModel = zendeskApiService.handleException(viewModel, e);
        }

        model.addAttribute("ticket", viewModel);

        return "ticket";
    }

    @GetMapping("showAll")
    public String allTicketsPage(@RequestParam(required = false) Integer page, Model model) {

        ViewModel<TicketList> viewModel = new ViewModel<>();
        TicketList ticketList = new TicketList();
        try {
            ticketList = zendeskApiService.getAllTickets(page);
            if(ticketList.getTickets().size() > 0) {
                viewModel.setStatus(true);
            } else {
                viewModel.setStatus(false);
                viewModel.setMessage("No tickets found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            viewModel = zendeskApiService.handleException(viewModel, e);
        }

        viewModel.setData(ticketList);
        model.addAttribute("ticket", viewModel);
        model.addAttribute("allTickets", viewModel);

        return "allTickets";
    }

}
