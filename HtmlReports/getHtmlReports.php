<?php
	include( 'config.php' );
	$failcount=0;
	$passcount=0;
	$totalcount=0;
	$row = 0;
	$bodyText="";

	if (($handle = fopen($report_path, "r")) !== FALSE) {
		while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
					
			$bodyText.="<tr>";
			$bodyText.="\n";
			$num = count($data);		
			if ($row==0){
				$bodyText.="<td align='center' ><strong> SN </td>\n";
			}
			else{
				$bodyText.="<td align='center' bgcolor='#FFFFFF'> $row </td>\n";
				if (strcasecmp($data[7],"FAIL")==0){			
					$failcount = $failcount + 1;				
				}
				else{
					$passcount = $passcount + 1;				
				}			
			}
			for ($c=0; $c < $num; $c++) {
				if (strcasecmp($data[$c],"FAIL")==0){		
					$bodyText.="<td align='center' bgcolor='#ff6666' > <a href='getDetailedResults.php?testname=$data[2]'  target='_blank' >$data[$c] </a></td>";
					$bodyText.="\n";
				}
				else{
					if ($row==0){
						
						$bodyText.="<td align='center' ><strong>$data[$c]</td>";
					}
					else{
						$bodyText.="<td align='center' bgcolor='#FFFFFF'> $data[$c] </td>";
					}
					$bodyText.="\n";
				}		
			}
			
			$bodyText.="</tr>";
			$bodyText.="\n";
			$row++;
		}
		fclose($handle);
	}
?>
<html>
<head>
<title>Test case Report</title>
<meta http-equiv="refresh" content="30" />
</head>
<body bgcolor="#C0C0C0">

<br>
<table align="center" cellpadding="0" cellspacing="0"  width="200" border="1" bgcolor="#FFFFFF" >
  
  <th bgcolor="#6495ED" colspan =3 align="center" ><strong>Summary Report</strong></th>
   <tr>  
    <td align="center" >Total</td>   
	<td align="center" >Pass</td>
	<td align="center" >Fail</td> 
  </tr>
  <tr> 
	<?php 
		$totalcount = $passcount+$failcount;
		echo "<td align='center' >$totalcount</td>"  ;
		echo "<td align='center' > <a href='getDetailedResults.php?testname=Passed'  target='_blank'>$passcount</td>";
		echo "<td align='center' > <a href='getDetailedResults.php?testname=Failed'  target='_blank'> $failcount</td>";	
	?>	 
  </tr> 

</table>
<a href='getDetailedResults.php?testname=All'  target='_blank' >Detailed Results</a>
<br>
<hr>
<br>

<table align="center" width="800" border="1" cellpadding="0" cellspacing="0" bgcolor="#6495ED">

	<?php 
	echo $bodyText;
	?>	
</table>


</body>
</html>