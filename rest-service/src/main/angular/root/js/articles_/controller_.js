angular
    .module('articleApp')
        .controller('articleController', function($scope, Restangular) {

            $scope.p = Restangular.all('articles').getList().then(function(articleList) {
                $scope.articleList = articleList
            });
});