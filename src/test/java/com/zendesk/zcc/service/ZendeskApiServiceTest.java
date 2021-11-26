package com.zendesk.zcc.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zendesk.zcc.ZccApplication;
import com.zendesk.zcc.domain.AppException;
import com.zendesk.zcc.domain.Ticket;
import com.zendesk.zcc.domain.TicketList;
import com.zendesk.zcc.domain.ViewModel;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;

@SpringBootTest(classes = ZccApplication.class)
class ZendeskApiServiceTest {


    @Autowired
    private ZendeskApiService zendeskApiService;

    @Test
    @DisplayName("Should return http header with encoded Authorization")
    void itShouldReturnHttpHeaderEncodedAuth() {
        // given
        String username = "abc@xyz.com/token";
        String token = "randomToken12345";
        String expectedEncodedAuth = "Basic YWJjQHh5ei5jb20vdG9rZW46cmFuZG9tVG9rZW4xMjM0NQ==";

        // when
        HttpHeaders httpHeaders = zendeskApiService.createBasicAuthHeader(username, token);

        // then
        Assertions.assertTrue(httpHeaders.get("Authorization").size() == 1);
        Assertions.assertEquals(expectedEncodedAuth, httpHeaders.get("Authorization").get(0));
    }

    @Test
    @DisplayName("Should throw AppException for invalid credentials")
    void itShouldThrowAppExceptionForInvalidCredentials() throws Exception {
        // given
        String url = "https://zccahmad.zendesk.com/api/v2/tickets/1";
        String username = "abc@xyz.com/token";
        String token = "randomToken12345";
        String expectedExceptionMessage = "Application facing authentication error with service provider";

        // when
        AppException exception = Assertions.assertThrows(AppException.class, () -> {
            zendeskApiService.getResponse(url, username, token);
        });

        // then
        Assertions.assertEquals(expectedExceptionMessage, exception.getMessage());

    }

    @Test
    @DisplayName("Should return Ticket Object from JsonNode")
    void itShouldTransformJsonNodeToTicketObject() throws JsonProcessingException {
        // given
        String ticketJsonString = "{\"id\": 1, \"subject\": \"test_subject\", \"description\": \"test_description\", \"status\": \"test_status\"}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode ticketJsonNode = mapper.readTree(ticketJsonString);

        // when
        Ticket expected = new Ticket("1", "test_subject", "test_description", "test_status");
        Ticket response = zendeskApiService.transformIntoTicket(ticketJsonNode);

        // then
        Assertions.assertEquals(expected, response);

    }

    @Test
    @DisplayName("Should return the page number from url")
    void itShouldReturnPageNumberFromUrl() {
        // given
        String pageUrl = "https://zccahmad.zendesk.com/api/v2/tickets.json?page=5";
        Integer expectedPageNumber = 5;

        // when
        Integer result = zendeskApiService.getPageNumberFromUrl(pageUrl);

        // then
        Assertions.assertEquals(expectedPageNumber, result);

    }

    @Test
    @DisplayName("Should return the proper pagination text")
    void itShouldReturnProperPaginationText() {
        // given
        Integer previousPage = 0, currentPage = 1, totalTickets = 78, perPageTicketCount = 25;
        String firstPage = "Showing 1 to 25 of 78";
        String secondPage = "Showing 26 to 50 of 78";
        String thirdPage = "Showing 51 to 75 of 78";
        String fourthPage = "Showing 76 to 78 of 78";

        // when
        String firstPageResult = zendeskApiService.getPaginationInfo(previousPage, currentPage,
                totalTickets, perPageTicketCount);
        String secondPageResult = zendeskApiService.getPaginationInfo(++previousPage, ++currentPage,
                totalTickets, perPageTicketCount);
        String thirdPageResult = zendeskApiService.getPaginationInfo(++previousPage, ++currentPage,
                totalTickets, perPageTicketCount);
        String fourthPageResult = zendeskApiService.getPaginationInfo(++previousPage, ++currentPage,
                totalTickets, perPageTicketCount);

        // then
        Assertions.assertEquals(firstPage, firstPageResult);
        Assertions.assertEquals(secondPage, secondPageResult);
        Assertions.assertEquals(thirdPage, thirdPageResult);
        Assertions.assertEquals(fourthPage, fourthPageResult);

    }

    @Test
    @DisplayName("Should return the zero as page number from url when invalid url passed")
    void itShouldReturnZeroWhenInvalidUrlPassed() {
        // given
        String invalidUrl = "";
        Integer expectedPageNumber = 0;

        // when
        Integer result = zendeskApiService.getPageNumberFromUrl(invalidUrl);

        // then
        Assertions.assertEquals(0, result);

    }

    @Test
    @DisplayName("Should return all the tickets")
    void itShouldReturnAllTickets() throws Exception {
        // given
        Integer page = 0;

        // when
        TicketList ticketList = zendeskApiService.getAllTickets(page);

        // then
        Assertions.assertNotNull(ticketList);
        Assertions.assertNotNull(ticketList.getTotalTickets());
        Assertions.assertTrue(ticketList.getTotalTickets() > 0);
        Assertions.assertNotNull(ticketList.getTickets());
        Assertions.assertNotNull(ticketList.getTickets().get(0));
        Assertions.assertInstanceOf(Ticket.class, ticketList.getTickets().get(0));

    }

    @Test
    @DisplayName("Should return empty list for invalid page")
    void itShouldReturnEmptyTicketListForInvalidPage() throws Exception {
        // given
        Integer page = 5000;

        // when
        TicketList ticketList = zendeskApiService.getAllTickets(page);

        // then
        Assertions.assertNotNull(ticketList);
        Assertions.assertTrue(ticketList.getTickets().size() == 0);

    }

    @Test
    @DisplayName("Should return only one ticket information")
    void itShouldReturnOnlyOneTicketInformation() throws Exception {
        // given
        String ticketId = "1";

        // when
        Ticket ticket = zendeskApiService.getTicket(ticketId);

        // then
        Assertions.assertNotNull(ticket);
        Assertions.assertEquals(ticketId, ticket.getId());
    }

    @Test
    @DisplayName("Should throw AppException for both when ticket id does not exists and invalid ticket id")
    void itShouldThrowAppExceptionWhenTicketIdDoesNotExistsOrInvalid() {
        // given
        String notExistsTicketId = "5000";
        String invalidTicketId = "-5000";
        String notExistsExpectedExceptionMessage = "Your searched information does not exists";
        String invalidExpectedExceptionMessage = "You have searched for an Invalid information";

        // when
        AppException notExistsException = Assertions.assertThrows(AppException.class, () -> {
            zendeskApiService.getTicket(notExistsTicketId);
        });
        AppException invalidException = Assertions.assertThrows(AppException.class, () -> {
            zendeskApiService.getTicket(invalidTicketId);
        });

        // then
        Assertions.assertEquals(notExistsExpectedExceptionMessage, notExistsException.getMessage());
        Assertions.assertEquals(invalidExpectedExceptionMessage, invalidException.getMessage());
    }


    @Test
    @DisplayName("Should return the message of AppException when app exception is the parameter of handleException(args...) method")
    public void itShouldReturnViewModelMessageOfAppExceptionForAppExceptionParameter() {
        // given
        AppException appException = new AppException("This is app Exception");

        // when
        ViewModel returnObjectWithAppException = zendeskApiService.handleException(new ViewModel(), appException);

        // then
        Assertions.assertNotNull(returnObjectWithAppException);
        Assertions.assertFalse(returnObjectWithAppException.isStatus());
        Assertions.assertEquals(appException.getMessage(), returnObjectWithAppException.getMessage());

    }

    @Test
    @DisplayName("Should return the generic message when AppException is not the parameter of handleException(args...) method")
    public void itShouldReturnViewModelGenericMessageForOtherException() {
        // given
        String expectedDefaultMessageForOtherException = "Application is facing internal issues. Please try later";
        Exception exception = new Exception("This is other Exception");

        // when
        ViewModel returnObjectWithOtherException = zendeskApiService.handleException(new ViewModel(), exception);

        // then
        Assertions.assertNotNull(returnObjectWithOtherException);
        Assertions.assertFalse(returnObjectWithOtherException.isStatus());
        Assertions.assertNotEquals(exception.getMessage(), returnObjectWithOtherException.getMessage());
        Assertions.assertEquals(expectedDefaultMessageForOtherException, returnObjectWithOtherException.getMessage());

    }

}