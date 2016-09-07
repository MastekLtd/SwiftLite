<?php
	include( 'config.php' );
	$testname = $_REQUEST['testname'];	
?>
<html>
<head>
<title>Test case Report</title>
</head>
<body bgcolor="#C0C0C0">

<table align="center" width="800" border="1" cellpadding="0" cellspacing="0" bgcolor="#C0C0C0">

<?php
	$row = 0;
	if (($handle = fopen($detailedresult_path, "r")) !== FALSE) {
		while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
					
			echo "<tr>";
			echo "\n";
			$num = count($data);		
			if ($row==0){
				echo "<td align='center' bgcolor='#6495ED' ><strong> SN </td>";
				echo "\n";
				for ($c=0; $c < $num; $c++) {						
					echo "<td align='center' bgcolor='#6495ED'><strong>$data[$c]</td>";					
					echo "\n";					
				}
			}
			else{
			
				switch ($testname) {
					case "All":
						echo "<td bgcolor='#FFFFFF'> $row </td>";
						echo "\n";
						for ($c=0; $c < $num; $c++) {						
							if (strcasecmp($data[$c],"FAIL")==0){								
									echo "<td bgcolor='#ff6666'> $data[$c] </td>";
								}
								else{
									echo "<td bgcolor='#FFFFFF'> $data[$c] </td>";
								}				
							echo "\n";					
						}
						break;
					case "Failed":
						
						if (strcasecmp($data[5],"FAIL")==0){
							echo "<td bgcolor='#FFFFFF'> $row </td>";
							echo "\n";						
							for ($c=0; $c < $num; $c++) {
								if (strcasecmp($data[$c],"FAIL")==0){								
									echo "<td bgcolor='#ff6666'> $data[$c] </td>";
								}
								else{
									echo "<td bgcolor='#FFFFFF'> $data[$c] </td>";
								}								
								echo "\n";					
							}
						}
						break;
					case "Passed":
						
						if (strcasecmp($data[5],"PASS")==0){
							echo "<td bgcolor='#FFFFFF'> $row </td>";
							echo "\n";						
							for ($c=0; $c < $num; $c++) {						
								echo "<td bgcolor='#FFFFFF'> $data[$c] </td>";			
								echo "\n";					
							}
						}				
						break;
					default:
						
						if (strcasecmp($data[1],$testname)==0){
							echo "<td bgcolor='#FFFFFF'> $row </td>";
							echo "\n";
							for ($c=0; $c < $num; $c++) {						
								if (strcasecmp($data[$c],"FAIL")==0){								
									echo "<td bgcolor='#ff6666'> $data[$c] </td>";
								}
								else{
									echo "<td bgcolor='#FFFFFF'> $data[$c] </td>";
								}		
								echo "\n";					
							}
						}				
						break;
				 }
			}		
			
			echo "</tr>";
			echo "\n";
			$row++;
		}
		fclose($handle);
	}
?>

</table>

</body>
</html>