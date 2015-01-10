angular
    .module("articleApp", [])
    .constant("baseUrl", "http://localhost:8080/articles")
        .controller("articleController", function($scope, $http, baseUrl) {
            $scope.displayMode = "list";
            $scope.currentArticle = null;

            $scope.listArticles = function() {
                $scope.get(baseUrl).success(function (data) {
                    $scope.articles = data;
                    console.log(data);
                });
            }

            /*$scope.listProducts = function() {
                $scope.products = [
                    {id: 0, url: "http://natureofcode.com/book/chapter-10-neural-networks/", content: "You can’t process me with a normal brain."},
                    {id: 1, url: "http://natureofcode.com/book/chapter-7-cellular-automata/", content: "To play life you must have a fairly large checkerboard and a plentiful supply of flat counters of two colors."},
                ]
            }*/

            $scope.deleteArticle = function(article) {
                $scope.articles.splice($scope.articles.indexOf(article), 1);
            }

            $scope.creatArticle = function(article) {
                $scope.articles.push(article);
                $scope.displayMode = "list";
            }

            $scope.updateArticle = function(article) {
                for(var i = 0; i < $scope.articles.length; i++) {
                    if ($scope.articles[i].id == article.id) {
                        $scope.articles[i] = article;
                        break;
                    }
                }
                $scope.displayMode = "list";
            }

            $scope.editOrCreateArticle = function(article) {
                $scope.currentArticle = article ? angular.copy(article) : {};
                $scope.displayMode = "edit";
            }
        });