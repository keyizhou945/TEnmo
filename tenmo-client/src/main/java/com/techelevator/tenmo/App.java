package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.util.BasicLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    // TODO: 2024/9/12
    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistoryAndDetail();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    /**
     * @Author Keyi Zhou
     * @Description be able to see my Account Balance
     * @Date 2024/8/18 14:47
     * @Param
     * @return
    **/
    private void viewCurrentBalance() {
        //Step 3
        try {
            String url = API_BASE_URL + "/account/balance/" + currentUser.getUser().getId();
            double balance = restTemplate.getForObject(url, double.class);
            consoleService.printBalance(balance);
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }
    }

    /**
     * @return
     * @Author Keyi Zhou
     * @Description Step 5,6:
     * 1.list the transfers list.
     * 2.view transfer detail
     * @Date 2024/8/18 17:12
     * @Param
     **/
    private void viewTransferHistoryAndDetail() {

        List<TransferDto> transferDtoList = viewTransferHistory();

        if (transferDtoList == null || transferDtoList.size() == 0) {
            return;
        }

        while (true) {
            int transferId = consoleService.promptForInt("Please enter transfer ID to view details (0 to cancel): ");
            if (transferId == 0) {
                break;
            }
            getTransferDetail(transferId);

        }

    }

    private List<TransferDto> viewTransferHistory() {
        List<TransferDto> transferDtoList = new ArrayList<>();
        try {
            String url = API_BASE_URL + "transfer/transfer history/" + currentUser.getUser().getId();
            TransferDto[] transferDtos = restTemplate.getForObject(url, TransferDto[].class);
            transferDtoList = Arrays.asList(transferDtos);
            consoleService.printTransferList(transferDtoList);
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }
        return transferDtoList;
    }

    private TransferDto getTransferDetail(Integer transferId) {

        String url = API_BASE_URL + "transfer/" + transferId;
        TransferDto transferDto = null;
        try {
            transferDto = restTemplate.getForObject(url, TransferDto.class);

            if (transferDto == null) {
                System.out.println("Could not find the transfer info ");
                return null;
            }
            consoleService.printTransferDetail(transferDto);


        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }

        return transferDto;
    }



    //approve or reject pending transfers

    /**
     * @return
     * @Author Keyi Zhou
     * @Description Step 8,9:  see my Pending transfers
     * @Date 2024/8/18 19:20
     * @Param
     **/
    private void viewPendingRequests() {

        List<TransferDto> transferDtoList = getPendingRequests();
        if (transferDtoList == null || transferDtoList.isEmpty()) {
            return;
        }

        while (true) {
            int transferId = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel):");
            if (transferId == 0) {
                break;
            }
            TransferDto transferDto = getTransferDetail(transferId);
            if (transferDto == null) {
                continue;
            }
            Integer userSelection = consoleService.promptForInt("Approve or reject pending transfer:1: Approve 2: Reject 0: Cancel");
            if (userSelection.equals(0)) {
                break;
            } else if (userSelection.equals(1)) {
                approveTransfer(transferId);
            } else if (userSelection.equals(2)) {
                rejectTransfer(transferId);
            } else {
                System.out.println("Invalid action.");
            }
        }
    }

    private void approveTransfer(Integer transferId) {
        try {
            String url = API_BASE_URL + "/transfer/approve/" + transferId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Integer> entity = new HttpEntity<>(currentUser.getUser().getId(), headers);
            restTemplate.put(url, entity, Void.class);
            System.out.println("Transfer approved successfully!");
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Unable to approve. Please check.");
        }
    }

    // Reject a transfer
    private void rejectTransfer(Integer transferId) {
        try {

            String url = API_BASE_URL + "/transfer/reject/" + transferId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Integer> entity = new HttpEntity<>(currentUser.getUser().getId(), headers);
            restTemplate.put(url, entity, Void.class);
            System.out.println("Transfer rejected successfully!");
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Unable to reject. Please check.");
        }
    }





    //show the pending requests
    private List<TransferDto> getPendingRequests() {
        List<TransferDto> transferDtoList = new ArrayList<>();
        try {
            String url = API_BASE_URL + "/transfer/Pending Transfers/" + currentUser.getUser().getId();
            TransferDto[] transferDto  = restTemplate.getForObject(url, TransferDto[].class);
            transferDtoList = Arrays.asList(transferDto);
            if (transferDtoList == null || transferDtoList.isEmpty()) {
                System.out.println("No Transfer History.");
            }
            else {
                consoleService.printTransferList(transferDtoList);
            }

        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }
        return transferDtoList;
    }

    /**
     * @Author Keyi Zhou
     * @Description STEP 4 :be able to send a transfer of a specific amount of TE Bucks to a registered user
     *      Logic:
     *          1.view and verify the user list
     *          2.entry and verify userId
     *          3.entry and verify amount
     *          4.send Bucks
     * @Date 2024/8/18 15:10
     * @Param
     * @return
    **/
    private void sendBucks() {
        //view and verify the user list
        List<User> userList = listUserList();
        if(userList == null || userList.isEmpty()){
            return;
        }

        //entry and verify the userId
        Integer userId = consoleService.promptForInt("Enter id of user you want to send money to (0 to cancel): ");
        if (userId.equals(0)){
            return;
        } else if (!isUserIdAvailable(userId,userList)){
            return;
        }

        //entry and verify amount
        Double amount = consoleService.promptForDouble("Enter amount: ");
        if(!isAmountAvailable(amount)) {
            return;
        }


        sendBucksHelper(userId, amount);
    }


    private void requestBucks() {
        //view and verify the user list
        List<User> userList = listUserList();
        if (userList == null || userList.size() == 0) {
            return;
        }

        Integer userId = consoleService.promptForInt("Enter ID of user you are requesting from (0 to cancel):");
        if (userId.equals(0)) {
            return;
        }
        if (!isUserIdAvailable(userId, userList)) {
            return;
        }


        Double amount = consoleService.promptForDouble("Enter amount:");
        if (!isAmountAvailable(amount)) {
            return;
        }

        requestBucks(userId, amount);

    }

    private List<User> listUserList() {
        List<User> userList = new ArrayList<>();
        try {
            String url = API_BASE_URL + "/user/listAll/";
            User[] users = restTemplate.getForObject(url, User[].class);
            userList = Arrays.asList(users);
            consoleService.printUserList(userList);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }
        return userList;
    }


    private void requestBucks(Integer userId, Double amount) {

        try {
            String url = API_BASE_URL + "/transfer/request";

            //fill entity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            BucksDto bucksDto = new BucksDto();
            bucksDto.setFromUserId(currentUser.getUser().getId());
            bucksDto.setToUserId(userId);
            bucksDto.setAmount(amount);

            HttpEntity<BucksDto> entity = new HttpEntity<>(bucksDto, headers);
            restTemplate.postForObject(url, entity, Integer.class);
            System.out.println("request successfully!");

        }catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }

    }

    private void sendBucksHelper(Integer userId, Double amount) {
        try {
            String url = API_BASE_URL + "/transfer/send";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            BucksDto bucksDto = new BucksDto();
            bucksDto.setFromUserId(currentUser.getUser().getId());
            bucksDto.setToUserId(userId);
            bucksDto.setAmount(amount);

            HttpEntity<BucksDto> entity = new HttpEntity<>(bucksDto, headers);
            restTemplate.postForObject(url, entity, Integer.class);
            System.out.println("Send bucks successfully!");
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
            System.out.println("Something wrong with the request");
        }

    }

    private boolean isAmountAvailable(Double amount) {
        if (amount > 0) {
            return true;
        } else {
            System.out.println("can't request a zero or negative amount.");
            return false;
        }
    }

    /**
     * @Author Keyi Zhou
     * @Description verify userId
     *      1) not allowed to select user themselves
     *      2)Verify that the user ID is in the user list.
     * @Date 2024/8/18 15:26
     * @Param userId
     * @Param userList
     * @return
    **/
    private boolean isUserIdAvailable(Integer userId, List<User> userList) {

        if (userId.equals(currentUser.getUser().getId())) {
            System.out.println("Selecting yourself is not allowed.");
            return false;
        }

        //Verify whether the ID exists."
        for (User user : userList) {
            if (userId.equals(user.getId())) {
                return true;
            }
        }
        System.out.println("userId is not correct!");
        return false;
    }



}
