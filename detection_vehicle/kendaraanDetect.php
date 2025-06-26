<?php
require "DataBase.php";

$db = new DataBase();

if (isset($_POST['image64'])) {
    if ($db->dbConnect()) {
        // Tanggal masuk otomatis menggunakan NOW() di query
        if ($db->kendaraanInput("kendaraan", null, $_POST['image64'])) {
            echo "Image Upload Success";
        } else {
            echo "Image Upload Failed: " . mysqli_error($db->connect);
        }
    } else {
        echo "Error: Database connection";
    }
} else {
    echo "No image data received";
}
?>