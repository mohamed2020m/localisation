<?php
if ($_SERVER["REQUEST_METHOD"] == "GET") {
    include_once './racine.php';
    include_once RACINE . './service/PositionService.php';
    loadAll();
}

function loadAll()
{
    $es = new PositionService();
    header('Content-type: application/json');
    echo json_encode($es->getAll());
}
