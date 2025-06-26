<?php
require "DataBaseConfig.php";

class DataBase
{
    public $connect;
    public $data;
    private $sql;
    protected $servername;
    protected $name;
    protected $password;
    protected $databasename;

    public function __construct()
    {
        $this->connect = null;
        $this->data = null;
        $this->sql = null;
        $dbc = new DataBaseConfig();
        $this->servername = $dbc->servername;
        $this->name = $dbc->name;
        $this->password = $dbc->password;
        $this->databasename = $dbc->databasename;
    }

    function dbConnect()
    {
        $this->connect = mysqli_connect($this->servername, $this->name, $this->password, $this->databasename);
        return $this->connect;
    }

    function prepareData($data)
    {
        return mysqli_real_escape_string($this->connect, stripslashes(htmlspecialchars($data)));
    }

    function logIn($table, $email, $password)
    {
        $email = $this->prepareData($email);
        $password = $this->prepareData($password);
        $this->sql = "select * from " . $table . " where email = '" . $email. "'";
        $result = mysqli_query($this->connect, $this->sql);
        $row = mysqli_fetch_assoc($result);
        if (mysqli_num_rows($result) != 0) {
            $dbemail = $row['email'];
            $dbpassword = $row['password'];
            if ($dbemail == $email && password_verify($password, $dbpassword)) {
                $login = true;
            } else $login = false;
        } else $login = false;

        return $login;
    }

    function signUp($table,  $name,$email, $password)
    {
        $name = $this->prepareData($name);
        $password = $this->prepareData($password);
        $email = $this->prepareData($email);
        $password = password_hash($password, PASSWORD_DEFAULT);
        $this->sql =
            "INSERT INTO " . $table . " ( name, password, email,created_at) VALUES ('" . $name . "','" . $password . "','" . $email . "', NOW())";
        if (mysqli_query($this->connect, $this->sql)) {
            return true;
        } else return false;
    }

    function kendaraanInput($table, $tanggalMasuk, $image64) {
        $this->sql = "INSERT INTO " . $table . " (image64) VALUES (?)";
        
        $stmt = $this->connect->prepare($this->sql);
        $stmt->bind_param("s", $image64);
        
        if ($stmt->execute()) {
            return true;
        } else {
            error_log("Database error: " . $stmt->error);
            return false;
        }
    }
       

}

?>
