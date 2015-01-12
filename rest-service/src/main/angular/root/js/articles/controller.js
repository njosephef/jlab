angular.module('articleApp')
.controller('articleController', function($scope, Restangular) {

    var baseArticles = Restangular.all('articles');

    baseArticles.getList().then(function(articles) {
        $scope.articles = articles
    });

//    var newArticle = {url: "xyz", content: "abc"};
//    baseArticles.post(newArticle);

    $scope.add = function() {
        baseArticles.post($scope.newArticle).then(function(newArticle) {
            $scope.articles.push(newArticle);
            $scope.newArticle = {url: "", content: ""};
        })
    }
});