<?php

class DataBaseConfig
{
    public $servername;
    public $name;
    public $password;
    public $databasename;

    public function __construct()
    {

        $this->servername = 'localhost';
        $this->name = 'root';
        $this->password = '';
        $this->databasename = 'vehicle_detection';

    }
}

?>
