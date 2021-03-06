/*
 * Copyright (c) Istituto Nazionale di Fisica Nucleare (INFN). 2016-2018
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

angular.module('dashboardApp').controller('GroupsController', GroupsController);

GroupsController.$inject = [ '$scope', '$rootScope', '$uibModal', '$state',
		'$filter', 'filterFilter', 'scimFactory', 'ModalService', 'Utils', 'toaster' ];

function GroupsController($scope, $rootScope, $uibModal, $state, $filter,
		filterFilter, scimFactory, ModalService, Utils, toaster) {

	var gc = this;

	// group data
	gc.groups = [];

	// filtered groups to show
	gc.filtered = [];
	// text to find to filter groups
	gc.searchText = "";

	// pagination controls
	gc.currentPage = 1;
	gc.entryLimit = 10; // items per page

	// functions
	gc.getAllGroups = getAllGroups;
	gc.getGroups = getGroups;
	gc.resetFilters = resetFilters;
	gc.rebuildFilteredList = rebuildFilteredList;

	// add group
	gc.openAddGroupDialog = openAddGroupDialog;

	// delete group
	gc.deleteGroup = deleteGroup;
	gc.removeGroupFromList = removeGroupFromList;

	gc.loadGroupList = loadGroupList;

	// Controller actions:
	gc.resetFilters();
	gc.loadGroupList(); // eval gc.groups

	function rebuildFilteredList() {
		
		gc.filtered = filterFilter(gc.groups, {'displayName': gc.searchText});
		gc.filtered = $filter('orderBy')(gc.filtered, "displayName", false);
	}

	function resetFilters() {
		gc.searchText = "";
	}

	$scope.$watch('gc.searchText', function() {

		gc.rebuildFilteredList();
	});

	function loadGroupList() {

		$rootScope.pageLoadingProgress = 0;
		gc.loadingModal = $uibModal
		.open({
			animation: false,
			templateUrl : '/resources/iam/template/dashboard/loading-modal.html'
		});

		gc.loadingModal.opened.then(function() {
			gc.getAllGroups();
		});
	}

	function getAllGroups() {

		gc.groups = [];
		gc.getGroups(1, gc.entryLimit);
	}

	function getGroups(startIndex, count) {

		scimFactory
				.getGroups(startIndex, count)
				.then(
						function(response) {
							angular.forEach(response.data.Resources, function(
									group) {
								gc.groups.push(group);
							});
							gc.rebuildFilteredList();
							if (response.data.totalResults > (response.data.startIndex - 1 + response.data.itemsPerPage)) {
								gc.getGroups(startIndex + count, count);
								$rootScope.pageLoadingProgress = Math.floor((startIndex + count) * 100 / response.data.totalResults);
							} else {
								$rootScope.pageLoadingProgress = 100;
								gc.loadingModal.dismiss("Cancel");
								$rootScope.groups = gc.groups;
							}
							
						}, function(error) {
							console.log("getGroups error", error);
							gc.loadingModal.dismiss("Error");
							$scope.operationResult = Utils.buildErrorOperationResult(error);
						});
	}

	function openAddGroupDialog() {
		var modalInstance = $uibModal.open({
			templateUrl : '/resources/iam/template/dashboard/groups/newgroup.html',
			controller : 'AddGroupController',
			controllerAs: 'addGroupCtrl'
		});
		modalInstance.result.then(function(createdGroup) {
			console.info(createdGroup);
			gc.groups.push(createdGroup);
			gc.loadGroupList()
			toaster.pop({
		          type: 'success',
		          body:
		              `Group '${createdGroup.displayName}' CREATED successfully`
		        });
		}, function() {
			console.info('Modal dismissed at: ', new Date());
		});
	}

	function removeGroupFromList(group) {

		var i = gc.groups.indexOf(group);
		gc.groups.splice(i, 1);

		gc.rebuildFilteredList();
	}

	function deleteGroup(group) {
		
		var modalOptions = {
			closeButtonText: 'Cancel',
			actionButtonText: 'Delete Group',
			headerText: "Delete Group «" + group.displayName + "»",
			bodyText: `Are you sure you want to delete group '${group.displayName}'?`
		};
		
		ModalService.showModal({}, modalOptions).then(
			function (){
				scimFactory.deleteGroup(group.id)
					.then(function(response) {
						gc.removeGroupFromList(group);
						$rootScope.loggedUser.totGroups = $rootScope.loggedUser.totGroups -1;
						toaster.pop({
					          type: 'success',
					          body:
					              `Group '${group.displayName}' DELETED successfully`
					        });
					}, function(error) {
						toaster.pop({
					          type: 'error',
					          body:
					              `${error.data.detail}`
					        });
					});
			});
	}
}