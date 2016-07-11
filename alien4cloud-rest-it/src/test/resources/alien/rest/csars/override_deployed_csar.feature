Feature: Try override a csar when it is used in a deployed topology
  Background:
    Given I am authenticated with "ADMIN" role
    And I upload the archive "tosca-normative-types-1.0.0-SNAPSHOT"
    And I upload the local archive "data/csars/topology_recovery_test/test-topo-recovery-types.yaml"
    And I upload the local archive "data/csars/topology_recovery_test/sample-topology-test-recovery.yml"
    And I upload a plugin
    And I create an orchestrator named "Mount doom orchestrator" and plugin id "alien4cloud-mock-paas-provider:1.0" and bean name "mock-orchestrator-factory"
    And I enable the orchestrator "Mount doom orchestrator"
    And I create a location named "Thark location" and infrastructure type "OpenStack" to the orchestrator "Mount doom orchestrator"
    And I create a resource of type "alien.nodes.mock.openstack.Flavor" named "Small" related to the location "Mount doom orchestrator"/"Thark location"
    And I update the property "id" to "1" for the resource named "Small" related to the location "Mount doom orchestrator"/"Thark location"
    And I create a resource of type "alien.nodes.mock.openstack.Image" named "Ubuntu" related to the location "Mount doom orchestrator"/"Thark location"
    And I update the property "id" to "img1" for the resource named "Ubuntu" related to the location "Mount doom orchestrator"/"Thark location"
  	And I autogenerate the on-demand resources for the location "Mount doom orchestrator"/"Thark location"

    And There are these users in the system
      | sangoku |
    And I add a role "APPLICATIONS_MANAGER" to user "sangoku"
  	And I add a role "DEPLOYER" to user "sangoku" on the resource type "LOCATION" named "Thark location"
    And I am authenticated with user named "sangoku"

   	And I pre register orchestrator properties
      | managementUrl | http://cloudifyurl:8099 |
      | numberBackup  | 1                       |
      | managerEmail  | admin@alien.fr          |
      

  @reset
  Scenario: Deploy a topology, try to override a dependency archive should fail
  	Given I create a new application with name "ALIEN" and description "test override deployed dependency" based on the template with name "test-recovery-topology"
    And I Set a unique location policy to "Mount doom orchestrator"/"Thark location" for all nodes
    When I deploy it
    Then I should receive a RestResponse with no error
    And The application's deployment must succeed
    Given I am authenticated with "ADMIN" role
    When I upload without checking the result the local archive "data/csars/topology_recovery_test/test-recovery-nodetype-deleted-types.yaml"
   	Then I should receive a RestResponse with an error code 508
    
      