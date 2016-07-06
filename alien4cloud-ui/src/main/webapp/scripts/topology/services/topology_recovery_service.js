// define the rest api elements to work with topology edition.
define(function (require) {
  'use strict';
  var modules = require('modules');
  var _ = require('lodash');
  var angular = require('angular');

  var ACTIONS = [{
        code: 1,
        name: 'APPLICATIONS.TOPOLOGY.RECOVERY.RECOVER.TITLE',
        description: 'APPLICATIONS.TOPOLOGY.RECOVERY.RECOVER.DESCRIPTION'
      },{
        code: 2,
        name: 'APPLICATIONS.TOPOLOGY.RECOVERY.RESET.TITLE',
        description: 'APPLICATIONS.TOPOLOGY.RECOVERY.RESET.DESCRIPTION'
      }];

  var RecoveryChoiceCtrl = ['$scope', '$modalInstance', '$resource', 'dependencies',
    function($scope, $modalInstance, $resource, dependencies) {
      $scope.choice = {};
      $scope.dependencies = dependencies;
      $scope.possibleActions = ACTIONS;
      $scope.choose = function(action) {
          $modalInstance.close(action);
      };

      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
  }];

  modules.get('a4c-topology-editor', ['ngResource']).factory('topologyRecoveryServices', ['$resource', '$modal', '$q', 'toaster', '$translate',
    function($resource, $modal, $q, toaster, $translate) {

      var getUpdatedDependencies = $resource('rest/latest/topologies/:topologyId/updatedDependencies').get;

      var resetTopology = $resource('rest/latest/topologies/:topologyId/reset', {}, {
        'reset': {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json; charset=UTF-8'
          }
        }
      }).reset;

      var recover = $resource('rest/latest/topologies/:topologyId/recover', {}, {
        'recover': {
          method: 'PUT',
          isArray: false,
          headers: {
            'Content-Type': 'application/json; charset=UTF-8'
          }
        }
      }).recover;


      var handleRecoveryChoice = function( choice, topologyId, updatedDependencies){
        switch (choice) {
          case 1:
            return recover({
              topologyId: topologyId
            }, angular.toJson(updatedDependencies)).$promise.then(function(topoDTO){
              return topoDTO;
            });
          case 2:
            return resetTopology({topologyId: topologyId}, undefined).$promise.then(function(topoDTO){
              return topoDTO;
            });
          default:
           return null;
        }

      };

      /** handle Modal form for recovery choices */
      var openRecoveryChoiceModal = function (dependencies) {
        var deferred = $q.defer();
        var modalInstance = $modal.open({
          templateUrl: 'views/topology/topology_recovery_modal.html',
          controller: RecoveryChoiceCtrl,
          resolve: {
            dependencies: function () {
              return dependencies;
            }
          }
        });
        modalInstance.result.then(function (recoveryChoice) {
          deferred.resolve(recoveryChoice);
        }, function(){
          //case the modal was dissmissed
          //this means do nothing with the topology
          deferred.resolve(-1);
        });
        return deferred.promise;
      };

      var handleDependenciesUpdates = function(topologyId) {
          return getUpdatedDependencies({topologyId: topologyId}).$promise.then(function(result){
            var updatedDependencies = result.data;

            //if no updated dependencies, then do nothing
            if(_.isEmpty(updatedDependencies)){
              return null;
            }

            return openRecoveryChoiceModal(updatedDependencies).then(function(choice){
              var result = handleRecoveryChoice(choice, topologyId, updatedDependencies);
              if(_.isFunction(_.get(result, 'then'))){
                return result.then(function(result){
                  toaster.pop('success', $translate.instant('APPLICATIONS.TOPOLOGY.RECOVERY.TITLE'), $translate.instant('APPLICATIONS.TOPOLOGY.RECOVERY.SUCCESS_MSGE'), 4000, 'trustedHtml', null);
                  return result;
                });
              }else {
                return null;
              }
            });
          });
      };

      return {
        'handleDependenciesUpdates': handleDependenciesUpdates
      };
    }
  ]);
}); // define
