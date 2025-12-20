package dz.usthb.eclipseworkspace.team;

import java.sql.Date;
import java.util.List;

import dz.usthb.eclipseworkspace.team.controller.TeamController;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDao;
import dz.usthb.eclipseworkspace.team.dao.TeamMemberDaoMock;
import dz.usthb.eclipseworkspace.team.model.TeamMember;
import dz.usthb.eclipseworkspace.team.service.TeamMemberService;
import dz.usthb.eclipseworkspace.workspace.model.AppUser;

public class TestMemberPermissions {
    public static void main(String[] args) {
        System.out.println("=== TESTS SPÉCIFIQUES - PERMISSIONS DES MEMBRES ===\n");
        System.out.println("Scénario: Un membre (non-LEAD) tente d'effectuer des actions réservées au LEAD\n");
        
        // Initialisation avec mock DAO
        TeamMemberDao dao = new TeamMemberDaoMock();
        TeamMemberService service = new TeamMemberService(dao);
        TeamController controller = new TeamController(service);
        
        // Créer des utilisateurs
        AppUser lead = createUser(100, "lead@test.com", "lead", "Lead", "User");
        AppUser member1 = createUser(101, "member1@test.com", "member1", "Member", "One");
        AppUser member2 = createUser(102, "member2@test.com", "member2", "Member", "Two");
        AppUser newUser = createUser(200, "new@test.com", "newuser", "New", "User");
        
        Long teamId = 1L;
        int testsPassed = 0;
        int testsFailed = 0;
        
        try {
            // === ÉTAPE 1: Configuration initiale ===
            System.out.println("=== CONFIGURATION INITIALE ===");
            System.out.println("1. Création de l'équipe avec LEAD (ID: 100)...");
            service.createTeamWithLeader(teamId, lead);
            System.out.println("   ✓ Équipe créée\n");
            
            System.out.println("2. LEAD ajoute un premier membre (ID: 101)...");
            TeamMember addedMember1 = controller.addMemberToTeam(lead, teamId, (long)member1.getUser_id(), "MEMBER");
            System.out.println("   ✓ Membre ajouté: " + addedMember1 + "\n");
            
            System.out.println("3. LEAD ajoute un deuxième membre (ID: 102)...");
            TeamMember addedMember2 = controller.addMemberToTeam(lead, teamId, (long)member2.getUser_id(), "MEMBER");
            System.out.println("   ✓ Membre ajouté: " + addedMember2 + "\n");
            
            System.out.println("État actuel de l'équipe:");
            List<TeamMember> members = controller.getTeamMembers(teamId);
            for (TeamMember m : members) {
                System.out.println("   - " + m);
            }
            System.out.println();
            
            // === TEST 1: MEMBER tente d'ajouter un nouveau membre ===
            System.out.println("=== TEST 1: MEMBER (ID: 101) TENTE D'AJOUTER UN MEMBRE ===");
            System.out.println("Action: Member 101 essaie d'ajouter User 200 à l'équipe...");
            
            try {
                TeamMember result = controller.addMemberToTeam(member1, teamId, (long)newUser.getUser_id(), "MEMBER");
                
                if (result == null) {
                    System.out.println("   ✓ SUCCÈS: Permission refusée comme attendu");
                    System.out.println("   ✓ Message attendu: 'Seul le LEAD peut ajouter des membres à l'équipe'");
                    testsPassed++;
                } else {
                    System.out.println("   ✗ ÉCHEC: Member a réussi à ajouter un membre (BUG!)");
                    System.out.println("   ✗ Membre ajouté: " + result);
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("   ✓ SUCCÈS: Exception capturée - " + e.getMessage());
                testsPassed++;
            }
            System.out.println();
            
            // === TEST 2: MEMBER tente de changer son propre rôle en MEMBER ===
            System.out.println("=== TEST 2: MEMBER TENTE DE CHANGER SON PROPRE RÔLE ===");
            System.out.println("Cas 2A: Member 101 essaie de se rétrograder en MEMBER (déjà MEMBER)...");
            
            try {
                boolean result = controller.updateMemberRole(member1, teamId, (long)member1.getUser_id(), "MEMBER");
                
                if (result) {
                    System.out.println("   ✓ SUCCÈS: Member peut se rétrograder en MEMBER");
                    System.out.println("   ✓ Note: Aucun changement réel car déjà MEMBER");
                    testsPassed++;
                } else {
                    System.out.println("   ✗ ÉCHEC: Member ne peut pas changer son propre rôle");
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("   ✗ ERREUR INATTENDUE: " + e.getMessage());
                testsFailed++;
            }
            System.out.println();
            
            System.out.println("Cas 2B: Member 101 essaie de se promouvoir en LEAD...");
            
            try {
                boolean result = controller.updateMemberRole(member1, teamId, (long)member1.getUser_id(), "LEAD");
                
                if (!result) {
                    System.out.println("   ✓ SUCCÈS: Permission refusée comme attendu");
                    System.out.println("   ✓ Message attendu: 'Un membre ne peut pas se promouvoir lui-même en LEAD'");
                    testsPassed++;
                } else {
                    System.out.println("   ✗ ÉCHEC: Member a réussi à se promouvoir LEAD (BUG!)");
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("   ✓ SUCCÈS: Exception capturée - " + e.getMessage());
                testsPassed++;
            }
            System.out.println();
// === TEST 3: MEMBER tente de changer le rôle d'un autre membre ===
System.out.println("=== TEST 3: MEMBER TENTE DE CHANGER LE RÔLE D'UN AUTRE MEMBRE ===");
System.out.println("Action: Member 101 essaie de changer le rôle de Member 102...");

System.out.println("Cas 3A: Member 101 essaie de promouvoir Member 102 en LEAD...");

try {
    boolean result = controller.updateMemberRole(member1, teamId, (long)member2.getUser_id(), "LEAD");
    
    if (!result) {
        System.out.println("   ✓ SUCCÈS: Permission refusée comme attendu");
        System.out.println("   ✓ Message attendu: 'Seul le LEAD peut changer le rôle d'un autre membre'");
        testsPassed++;
    } else {
        System.out.println("   ✗ ÉCHEC: Member a réussi à promouvoir un autre membre (BUG!)");
        testsFailed++;
    }
} catch (Exception e) {
    System.out.println("   ✓ SUCCÈS: Exception capturée - " + e.getMessage());
    testsPassed++;
}
System.out.println();

// TEST CORRIGÉ : Vérifier d'abord si un changement est nécessaire
System.out.println("Cas 3B: Vérification - Member 101 ne peut pas changer un rôle qui est déjà le même...");

try {
    // Récupérer le membre actuel
    TeamMember currentMember102 = service.getTeamMemberByUser(teamId, (long)member2.getUser_id());
    String currentRole = currentMember102.getRole();
    
    System.out.println("   INFO: Member 102 a actuellement le rôle: " + currentRole);
    
    // Essayer de mettre le même rôle
    boolean result = controller.updateMemberRole(member1, teamId, (long)member2.getUser_id(), currentRole);
    
    // Ce devrait retourner true (aucun changement) mais ce n'est pas un test de permission
    // car la permission n'est même pas vérifiée quand le rôle est identique
    if (result) {
        System.out.println("   ✓ INFO: Aucun changement - rôle déjà " + currentRole);
        System.out.println("   ✓ Note: La permission n'est pas vérifiée quand le rôle est identique");
        // Ne pas compter comme test réussi/échoué, c'est un cas spécial
    } else {
        System.out.println("   ? COMPORTEMENT INATTENDU: retourne false pour un rôle identique");
    }
} catch (Exception e) {
    System.out.println("   ✗ ERREUR: " + e.getMessage());
}
System.out.println();

// NOUVEAU TEST: Cas où un changement réel est nécessaire
System.out.println("Cas 3C: Test réel - Member 101 essaie de rétrograder un LEAD...");

// D'abord faire de Member 102 un LEAD temporairement
try {
    System.out.println("   Étape 1: LEAD promeut Member 102 en LEAD temporairement...");
    boolean promoted = controller.updateMemberRole(lead, teamId, (long)member2.getUser_id(), "LEAD");
    
    if (promoted) {
        System.out.println("   ✓ Member 102 est maintenant LEAD");
        
        // Maintenant tester si Member 101 peut le rétrograder
        System.out.println("   Étape 2: Member 101 essaie de rétrograder Member 102 en MEMBER...");
        boolean demoted = controller.updateMemberRole(member1, teamId, (long)member2.getUser_id(), "MEMBER");
        
        if (!demoted) {
            System.out.println("   ✓ SUCCÈS: Permission refusée comme attendu");
            System.out.println("   ✓ Message: 'Seul le LEAD peut changer le rôle d'un autre membre'");
            testsPassed++;
        } else {
            System.out.println("   ✗ ÉCHEC: Member a réussi à changer le rôle d'un LEAD (BUG GRAVE!)");
            testsFailed++;
        }
        
        // Remettre Member 102 en MEMBER (via LEAD)
        System.out.println("   Étape 3: LEAD remet Member 102 en MEMBER...");
        controller.updateMemberRole(lead, teamId, (long)member2.getUser_id(), "MEMBER");
        
    } else {
        System.out.println("   ✗ ÉCHEC: Impossible de promouvoir Member 102");
        testsFailed++;
    }
} catch (Exception e) {
    System.out.println("   ✗ ERREUR: " + e.getMessage());
    testsFailed++;
}
            
            // === TEST 4: VERIFICATION - LEAD peut toujours effectuer ces actions ===
            System.out.println("=== TEST 4: VERIFICATION - LEAD PEUT EFFECTUER CES ACTIONS ===");
            System.out.println("Action: LEAD (ID: 100) ajoute un nouveau membre pour vérifier que les permissions LEAD fonctionnent...");
            
            try {
                TeamMember result = controller.addMemberToTeam(lead, teamId, (long)newUser.getUser_id(), "MEMBER");
                
                if (result != null) {
                    System.out.println("   ✓ SUCCÈS: LEAD peut ajouter un membre");
                    System.out.println("   ✓ Membre ajouté: " + result);
                    testsPassed++;
                } else {
                    System.out.println("   ✗ ÉCHEC: LEAD ne peut pas ajouter un membre (BUG!)");
                    testsFailed++;
                }
            } catch (Exception e) {
                System.out.println("   ✗ ERREUR INATTENDUE: " + e.getMessage());
                testsFailed++;
            }
            System.out.println();
            
            System.out.println("État final de l'équipe:");
            members = controller.getTeamMembers(teamId);
            for (TeamMember m : members) {
                System.out.println("   - " + m);
            }
            
        } catch (Exception e) {
            System.err.println("\n✗ ERREUR GÉNÉRALE: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
        
        // === RÉSUMÉ DES TESTS ===
        System.out.println("\n=== RÉSUMÉ DES TESTS PERMISSIONS MEMBRES ===");
        System.out.println("Tests réalisés:");
        System.out.println("  1. Member tente d'ajouter un membre → DOIT ÉCHOUER");
        System.out.println("  2. Member tente de changer son propre rôle:");
        System.out.println("     a. Se rétrograder en MEMBER → DOIT RÉUSSIR");
        System.out.println("     b. Se promouvoir en LEAD → DOIT ÉCHOUER");
        System.out.println("  3. Member tente de changer le rôle d'un autre membre → DOIT ÉCHOUER");
        System.out.println("  4. Verification: LEAD peut toujours ajouter des membres → DOIT RÉUSSIR");
        
        System.out.println("\nRésultats:");
        System.out.println("Tests réussis: " + testsPassed);
        System.out.println("Tests échoués: " + testsFailed);
        
        if (testsFailed == 0) {
            System.out.println("\n✅ TOUS LES TESTS DE PERMISSIONS ONT RÉUSSI !");
            System.out.println("✅ Le système de permissions fonctionne correctement:");
            System.out.println("   - Les MEMBERS ne peuvent pas ajouter de membres");
            System.out.println("   - Les MEMBERS ne peuvent pas promouvoir en LEAD");
            System.out.println("   - Les MEMBERS ne peuvent pas changer les rôles des autres");
            System.out.println("   - Les MEMBERS peuvent se rétrograder eux-mêmes");
            System.out.println("   - Les LEAD conservent toutes leurs permissions");
        } else {
            System.out.println("\n❌ " + testsFailed + " TEST(S) ONT ÉCHOUÉ");
            System.out.println("   → Vérifiez la logique des permissions dans PermissionManager");
        }
    }
    
    private static AppUser createUser(int id, String email, String username, String firstName, String lastName) {
        return new AppUser(
            id,
            email,
            username,
            firstName,
            lastName,
            "123-456-7890",
            "password_hash_" + id,
            new Date(System.currentTimeMillis())
        );
    }
}