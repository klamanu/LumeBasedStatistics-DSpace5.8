package org.dspace.app.xmlui.statistics;

public final class Country 
{
    
    public static final String COUNTRY_KEY_TO_TRANSLATE = "xmlui.Stats.country.";

	public final static String[] countryCode = 
	{
		"--","AP","EU","AD","AE","AF","AG","AI","AL","AM","AN","AO","AQ","AR",
		"AS","AT","AU","AW","AZ","BA","BB","BD","BE","BF","BG","BH","BI","BJ",
		"BM","BN","BO","BR","BS","BT","BV","BW","BY","BZ","CA","CC","CD","CF",
		"CG","CH","CI","CK","CL","CM","CN","CO","CR","CU","CV","CX","CY","CZ",
		"DE","DJ","DK","DM","DO","DZ","EC","EE","EG","EH","ER","ES","ET","FI",
		"FJ","FK","FM","FO","FR","FX","GA","GB","GD","GE","GF","GH","GI","GL",
		"GM","GN","GP","GQ","GR","GS","GT","GU","GW","GY","HK","HM","HN","HR",
		"HT","HU","ID","IE","IL","IN","IO","IQ","IR","IS","IT","JM","JO","JP",
		"KE","KG","KH","KI","KM","KN","KP","KR","KW","KY","KZ","LA","LB","LC",
		"LI","LK","LR","LS","LV","LU","LT","LY","MA","MC","MD","MG","MH","MK",
		"ML","MM","MN","MO","MP","MQ","MR","MS","MT","MU","MV","MW","MX","MY",
		"MZ","NA","NC","NE","NF","NG","NI","NL","NO","NP","NR","NU","NZ","OM",
		"PA","PE","PF","PG","PH","PK","PL","PM","PN","PR","PS","PT","PW","PY",
		"QA","RE","RO","RU","RW","SA","SB","SC","SD","SE","SG","SH","SI","SJ",
		"SK","SL","SM","SN","SO","SR","ST","SV","SY","SZ","TC","TD","TF","TG",
		"TH","TJ","TK","TM","TN","TO","TL","TR","TT","TV","TW","TZ","UA","UG",
		"UM","US","UY","UZ","VA","VC","VE","VG","VI","VN","VU","WF","WS","YE",
		"YT","RS","ZA","ZM","ME","ZW","A1","A2","O1","AX","GG","IM","JE","BL",
		"MF","UFRGS"
	};
    
	public final static String[] countryName = 
	{
		"Outros","Região da Ásia/Pacífico","Europa","Andorra","Emirados Árabes Unidos",
		"Afeganistão","Antígua e Barbuda","Anguilla","Albânia","Armênia",
		"Antilhas Holandesas","Angola","Antarctica","Argentina","Samoa Americana",
		"Áustria","Austrália","Aruba","Azerbaijão","Bósnia-Herzegovina",
		"Barbados","Bangladesh","Bélgica","Burkina Faso","Bulgária","Bahrein",
		"Burundi","Benin","Bermuda","Brunei Darussalam","Bolívia","Brasil","Bahamas",
		"Butão","Bouvet Island","Botswana","Bielorússia","Belize","Canadá",
		"Cocos (Keeling) Islands","Republica Democrática do Congo",
		"República Centro-Africana","Congo","Suíça","Costa do Marfim",
		"Ilhas Cook","Chile","Camarões","China","Colômbia","Costa Rica","Cuba",
		"Cabo Verde","Christmas Island","Chipre","República Checa","Alemanha",
		"Djibouti","Dinamarca","Dominica","República Dominicana","Argélia","Equador",
		"Estônia","Egito","Saara Ocidental","Eritréia","Espanha","Etiópia","Finlândia",
		"Fiji","Ilhas Falkland","Estados Federados da Micron?sia",
		"Ilhas Faroe","França","França Metropolitana","Gabão","Reino Unido",
		"Granada","Geórgia","Guiana Francesa","Gana","Gibraltar","Groenlândia","Gâmbia",
		"Guiné","Guadalupe","Guiné Equatorial","Grécia",
		"South Georgia and the South Sandwich Islands","Guatemala","Guam",
		"Guiné-Bissau","Guiana","Hong Kong","Heard Island and McDonald Islands",
		"Honduras","Croácia","Haiti","Hungria","Indonésia","Irlanda","Israel","Índia",
		"British Indian Ocean Territory","Iraque","Irã",
		"Islândia","Itália","Jamaica","Jordânia","Japão","Quênia","Quirguistão","Camboja",
		"Kiribati","Comoros","São Cristóvão e Nevis",
		"Coréia do Norte","Coréia do Sul","Kuwait",
		"Ilhas Cayman","Cazaquistão","Laos","Líbano",
		"Santa Lúcia","Liechtenstein","Sri Lanka","Libéria","Lesoto","Letônia",
		"Luxemburgo","Lituânia","Líbia","Marrocos","Mônaco",
		"Moldova","Madagascar","Ilhas Marshall",
		"Macedônia","Mali","Myanmar","Mongólia",
		"Macau","Northern Mariana Islands","Martinica","Mauritânia","Montserrat",
		"Malta","Maurício","Maldivas","Malawi","México","Malásia","Moçambique",
		"Namíbia","Nova Caledônia","Níger","Norfolk Island","Nigéria","Nicarágua",
		"Holanda","Noruega","Nepal","Nauru","Niue","Nova Zelândia","Omã","Panamá",
		"Peru","Polinésia Francesa","Papua Nova Guiné","Filipinas","Paquistão",
		"Polônia","Saint Pierre and Miquelon","Pitcairn Islands","Porto Rico","" +
		"Território Palestino","Portugal","Palau","Paraguai","Catar",
		"Reunião","Romênia","Rússia","Ruanda","Arábia Saudita",
		"Ilhas Salomão","Seychelles","Sudão","Suécia","Cingapura","Santa Helena",
		"Eslovênia","Svalbard and Jan Mayen","Eslováquia","Serra Leoa","San Marino",
		"Senegal","Somália","Suriname","São Tomé e Príncipe","El Salvador",
		"Síria","Suazilândia","Turks and Caicos Islands","Chade",
		"French Southern Territories","Togo","Tailândia","Tadjiquistão","Tokelau",
		"Turcomenistão","Tunísia","Tonga","Timor Leste","Turquia","Trinidad e Tobago",
		"Tuvalu","Taiwan","Tanzânia","Ucrânia","Uganda",
		"United States Minor Outlying Islands","Estados Unidos","Uruguai","Uzbequistão",
		"Cidade do Vaticano","São Vicente e Granadina",
		"Venezuela","Ilhas Virgens Britânicas","Ilhas Virgens Americanas","Vietnã",
		"Vanuatu","Wallis and Futuna","Samoa","Iêmen","Mayotte","Sérvia",
		"África do Sul","Zâmbia","Montenegro","Zimbábue","Proxy Anônimo",
		"Provedor Satélite","Outros","Aland Islands","Guernsey","Ilha de Man","Jersey",
		"São Bartolomeu","São Martin","UFRGS*"
	}; 
}