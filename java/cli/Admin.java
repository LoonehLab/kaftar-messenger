package cli;

import dao.*;
import entity.*;
import security.PasswordHasher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Admin implements Runnable {

    private static final UserRepository userRepo = UserRepository.getInstance();
    private static final AllGroupsRepository groupRepo = AllGroupsRepository.getInstance();
    private static final Scanner scanner = new Scanner(System.in);

    private static final String ADMIN_USER = "poori";
    private static final String ADMIN_PASS = "popopo";

    @Override
    public void run() {
        System.out.println("\n--- Welcome to Kaftar Server CLI Admin Panel ---");
        if (!authenticate()) {
            System.out.println("Authentication failed. CLI exiting...");
            return;
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listUsers(); break;
                case "2": addUser(); break;
                case "3": removeUser(); break;
                case "4": listGroupsAndMembers(); break;
                case "5": addGroup(); break;
                case "6": removeGroup(); break;
                case "7": manageGroupMembers(); break;
                case "8": viewReportedMessages(); break;
                case "0":
                    running = false;
                    System.out.println("Exiting Admin CLI...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private boolean authenticate() {
        System.out.print("Enter Admin Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine().trim();
        return ADMIN_USER.equals(username) && ADMIN_PASS.equals(password);
    }

    private void printMenu() {
        System.out.println("\n================ ADMIN MENU ================");
        System.out.println("1. List Users");
        System.out.println("2. Add User");
        System.out.println("3. Remove User");
        System.out.println("4. List Group Members");
        System.out.println("5. Add Group");
        System.out.println("6. Remove Group");
        System.out.println("7. Add/Remove Group Users");
        System.out.println("8. View Reported Messages");
        System.out.println("0. Exit CLI");
        System.out.print("Select an option: ");
    }

    private void listUsers() {
        List<User> users = userRepo.getAllUsers();
        System.out.println("\n--- User List ---");
        if (users == null || users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        for (User u : users) {
            System.out.printf("ID: %s | Username: %s | Full Name: %s | Phone: %s\n", 
                    u.getId(), u.getUserName(), u.getFullName(), u.getPhoneNumber());
        }
    }

    private void addUser() {
        System.out.println("\n--- Add New User ---");
        System.out.print("Enter Username: ");
        String user = scanner.nextLine().trim();
        System.out.print("Enter Full Name: ");
        String fullName = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String pass = scanner.nextLine().trim();
        String hashedPass = PasswordHasher.hashPassword(pass);
        User newUser = new User(user, fullName, hashedPass);
        userRepo.save(newUser);
        System.out.println("User registered successfully via CLI with ID: " + newUser.getId());
    }

    private void removeUser() {
        System.out.print("\nEnter User ID to remove: ");
        String userId = scanner.nextLine().trim();
        User user = userRepo.findUserById(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        List<Group> userGroups = new ArrayList<>(user.getGroups());
        for (Group g : userGroups) {
            g.leftOrRemove(user);
        }
        //Needs deleting
        System.out.println("User " + user.getUserName() + " has been disconnected from groups and marked for deletion.");
    }

    private void listGroupsAndMembers() {
        System.out.println("\n--- Group List & Members ---");
        List<Group> allGroups = groupRepo.findAll();
        for (Group g : allGroups) {
            System.out.printf("\n[%s] Group Name: %s | Owner: %s\n", 
                    g.getId(), g.getName(), g.getOwner().getUserName());
            System.out.println("  -> Members:");
            for (User member : g.getMembers()) {
                System.out.printf("     - [%s] %s (%s)\n", member.getId(), member.getFullName(), member.getUserName());
            }
        }
    }

    private void addGroup() {
        System.out.println("\n--- Create New Group ---");
        System.out.print("Enter Owner User ID: ");
        String ownerId = scanner.nextLine().trim();
        User owner = userRepo.findUserById(ownerId);
        if (owner == null) {
            System.out.println("Owner user not found.");
            return;
        }
        System.out.print("Enter Group Name: ");
        String groupName = scanner.nextLine().trim();
        Group newGroup = new Group(owner, groupName, owner);
        groupRepo.save(newGroup);
        owner.getInformationRepository().saveGroup(newGroup);
        System.out.println("Group created successfully via CLI with ID: " + newGroup.getId());
    }

    private void removeGroup() {
        System.out.print("\nEnter Group ID to delete: ");
        String groupId = scanner.nextLine().trim();
        Group targetGroup = groupRepo.findById(groupId);
        if (targetGroup == null) {
            System.out.println("Group not found.");
            return;
        }
        targetGroup.deleteGroupForAll();
        System.out.println("Group deleted for all members successfully.");
    }

    private void manageGroupMembers() {
        System.out.print("\nEnter Group ID: ");
        String groupId = scanner.nextLine().trim();
        Group targetGroup = groupRepo.findById(groupId);
        if (targetGroup == null) {
            System.out.println("Group not found.");
            return;
        }
        System.out.println("1. Add User to Group");
        System.out.println("2. Remove User from Group");
        System.out.print("Choose action: ");
        String action = scanner.nextLine().trim();
        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine().trim();
        User user = userRepo.findUserById(userId);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if ("1".equals(action)) {
            targetGroup.addMember(user);
            user.addGroup(targetGroup);
            user.getInformationRepository().saveGroup(targetGroup);
            System.out.println("User " + user.getUserName() + " added to group successfully.");
        }
        else if ("2".equals(action)) {
            targetGroup.leftOrRemove(user);
            System.out.println("User " + user.getUserName() + " removed from group successfully.");
        }
        else System.out.println("Invalid action selected.");
    }

    private void viewReportedMessages() {
        System.out.println("\n--- Reported Messages ---");
        ReportRepository reportRepo = ReportRepository.getInstance();
        Map <Integer, String[]> reports = reportRepo.getReports();
        if (reports == null || reports.isEmpty()) {
            System.out.println("No reported messages found.");
            return;
        }
        for (Map.Entry<Integer, String[]> entry : reports.entrySet()) {
            Integer msgId = entry.getKey();
            String[] reportData = entry.getValue();
            System.out.printf("Message ID: %d\n", msgId);
            if (reportData != null && reportData.length > 0) {
                String sender = reportData.length > 0 ? reportData[0] : "Unknown";
                String content = reportData.length > 1 ? reportData[1] : "No Content";
                System.out.printf("  -> Sender ID: %s", sender);
                System.out.printf(" , Content: \"%s\"\n", content);
            }
            else {
                System.out.println("  -> No details available.");
            }
            System.out.println("----------------------");
        }
        
        System.out.println("Admin Actions:");
        System.out.println("1. Dismiss / Delete a Report");
        System.out.println("0. Back to Main Menu");
        System.out.print("Select action: ");
        String action = scanner.nextLine().trim();
        
        if ("1".equals(action)) {
            System.out.print("Enter the Message ID to dismiss/delete: ");
            String idInput = scanner.nextLine().trim();
            try {
                int targetMsgId = Integer.parseInt(idInput);
                
                if (reports.containsKey(targetMsgId)) {
                    String[] reportData = reports.get(targetMsgId);
                    String actualSender = (reportData != null && reportData.length > 0) ? reportData[0] : "Unknown";
                    System.out.print("Enter the Sender ID to confirm: ");
                    String senderInput = scanner.nextLine().trim();
                    if (actualSender.equals(senderInput)) {
                        reportRepo.delete(targetMsgId);
                        System.out.println("Report for message ID " + targetMsgId + " from sender " + senderInput + " has been successfully cleared.");
                    }
                    else System.out.println("Error: Sender ID does not match the report record. Action aborted.");
                }
                else System.out.println("Error: Message ID not found in current reports.");
            }
            catch (NumberFormatException e) {
                System.out.println("Invalid ID format. Please enter a number.");
            }
        }
    }
}
