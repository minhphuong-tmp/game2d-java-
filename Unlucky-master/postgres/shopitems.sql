DROP TABLE IF EXISTS ShopItems;
CREATE TABLE ShopItems(
   rarity   INTEGER  NOT NULL
  ,name     VARCHAR(25) NOT NULL
  ,description     VARCHAR(56) NOT NULL
  ,type     INTEGER  NOT NULL
  ,imgIndex INTEGER  NOT NULL
  ,hp       INTEGER 
  ,exp      INTEGER 
  ,sell     INTEGER  NOT NULL
  ,price    INTEGER  NOT NULL
  ,mhp      INTEGER 
  ,dmg      INTEGER 
  ,acc      INTEGER 
  ,eChance  INTEGER 
  ,PRIMARY KEY(type,imgIndex)
);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Bình máu nhỏ','Khôi phục một ít HP..',0,0,30,0,50,155,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Bình máu vừa','Khôi phục một lượng HP kha khá..',0,1,100,0,75,270,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ pháp sư','Chiếc mũ chứa đầy trò ảo thuật.',2,0,NULL,NULL,25,100,4,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ nồi xanh','Thường thấy trên đầu người Pháp.',2,1,NULL,NULL,30,160,6,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ ếch','Một chiếc đầu ếch vui nhộn.',2,2,NULL,NULL,40,200,7,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ tiệc tùng','Người đội nó sẽ cực vui ở mọi bữa tiệc.',2,3,NULL,NULL,40,200,7,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ đồng','Mũ cấp thấp.',2,4,NULL,NULL,275,730,14,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Mũ sắt','Mũ cấp trung.',2,5,NULL,NULL,450,1800,24,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Áo choàng pháp sư','Áo choàng thấm nhuần phép thuật.',3,0,NULL,NULL,45,125,7,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Áo gile và sơ mi xanh','Bộ đồ sang trọng cho dịp đặc biệt.',3,1,NULL,NULL,50,170,8,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Đồ ếch','Trang phục ếch vui nhộn.',3,2,NULL,NULL,80,220,10,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giáp da du kích','Dành cho cung thủ lão luyện.',3,3,NULL,NULL,175,600,14,0,1,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giáp ngực đồng','Giáp ngực cấp thấp.',3,4,NULL,NULL,300,900,22,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Gip ngực sắt','Giáp ngực cấp trung.',3,5,NULL,NULL,550,2050,40,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Đũa phép','Gậy phép thuật nhỏ.',4,0,NULL,NULL,40,170,0,4,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Cung gỗ','Cung thủ gỗ chuẩn xác..',4,1,NULL,NULL,70,500,0,6,1,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Cần câu','Quăng móc kéo kẻ địch lại.',4,2,NULL,NULL,45,200,0,6,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Kiếm gỗ','Không bền lắm.',4,3,NULL,NULL,60,250,0,9,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Kiếm đồng','Kiếm cấp thấp.',4,4,NULL,NULL,280,800,0,18,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Kiếm sắt','Kiếm cấp trung..',4,5,NULL,NULL,560,2200,0,27,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Găng pháp sư','Đôi găng phép thuật.',5,0,NULL,NULL,25,100,4,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Găng da','Dành cho tân binh.',5,1,NULL,NULL,30,120,5,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Găng đồng','Găng cấp thấp.',5,2,NULL,NULL,275,680,14,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Găng sắt','Găng cấp trung.',5,3,NULL,NULL,430,1790,22,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giày pháp sư','Đôi giày ma thuật.',6,0,NULL,NULL,25,100,4,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giày da','Dành cho tân binh.',6,1,NULL,NULL,30,120,5,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giày đồng','Giày cấp thấp.',6,2,NULL,NULL,275,680,14,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Giày sắt','Giày cấp trung.',6,3,NULL,NULL,430,1790,22,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Khiên gỗ','Bảo vệ chút ít.',8,0,NULL,NULL,30,180,5,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Khiên đồng','Khiên cấp thấp.',8,1,NULL,NULL,325,700,18,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (0,'Khiên sắt','Khiên cấp trung.',8,2,NULL,NULL,500,2000,30,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Bình máu lớn','Khôi phục nhiều HP..',0,2,175,0,150,520,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Mũ vàng','Mũ cấp cao.',2,6,NULL,NULL,800,3600,40,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Giáp ngực vàng','Giáp ngực cấp cao.',3,6,NULL,NULL,900,3850,66,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Kiếm vàng','Kiếm cấp cao.',4,6,NULL,NULL,1000,4000,0,48,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Găng vàng','Găng cấp cao..',5,4,NULL,NULL,680,3500,38,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Giày vàng','Giày cấp cao..',6,4,NULL,NULL,680,3500,38,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Khiên vàng','Khiên cấp cao.',8,3,NULL,NULL,800,3700,50,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Dây chuyền bạc','Không rõ tác dụng.',7,1,NULL,NULL,900,1800,0,0,1,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Dây chuyền thống trị','Tăng sức mạnh.',7,2,NULL,NULL,3600,20000,0,30,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Dây chuyền hộ mệnh','Tăng phòng thủ.',7,4,NULL,NULL,2000,12500,30,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Nhẫn vàng','Nhẫn chính xác.',9,0,NULL,NULL,1000,2050,0,0,1,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Nhẫn bạc','Không rõ tác dụng.',9,1,NULL,NULL,250,700,0,1,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (1,'Nhẫn hủy diệt','Truyền sức mạnh hủy diệt.',9,2,NULL,NULL,2000,12500,0,20,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Bình máu đặc biệt','Khôi phục lượng HP khổng lồ.',0,3,-30,0,400,950,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Bình máu phù phép','Khôi phục lượng HP cực lớn.',0,4,-40,0,500,1100,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Bình kinh nghiệm','Tăng một ít kinh nghiệm.',0,7,0,3,10000,120000,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Mũ khắc rune','Được truyền sức mạnh rune.',2,7,NULL,NULL,2200,12000,68,5,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Giáp ngực rune','Được truyền năng lượng rune.',3,7,NULL,NULL,2900,13500,96,10,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Gươm rune','Được truyền năng lượng rune.',4,7,NULL,NULL,3000,15000,0,78,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Găng rune','Truyền sức mạnh rune.',5,5,NULL,NULL,2600,11000,64,3,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Giày rune','Truyền sức mạnh rune.',6,5,NULL,NULL,2600,11000,64,3,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Khiên rune','Truyền năng lượng rune.',8,4,NULL,NULL,3000,13000,80,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Khiên sọ','Chế từ hài cốt khai quật.',8,6,NULL,NULL,4000,18000,100,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Dây chuyền vàng','Tăng độ chính xác.',7,0,NULL,NULL,3000,11400,0,0,2,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Dây chuyền quỷ ám','Bị nguyền rủa bởi ác quỷ.',7,3,NULL,NULL,4500,25000,30,15,1,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Dây chuyền “Trái ngược mà cân bằng”','Có một sợi dây chuyền chị em.',7,5,NULL,NULL,12000,85000,70,30,2,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Dây chuyền “Cân bằng mà trái ngược”','Có một sợi dây chuyền chị em.',7,6,NULL,NULL,12000,85000,70,30,2,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Nhẫn tàn phá','Ban sức mạnh hủy diệt.',9,3,NULL,NULL,6900,38000,0,40,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (2,'Nhẫn sinh mệnh lớn','Bảo vệ chủ nhân khỏi hiểm nguy.',9,4,NULL,NULL,8000,55000,100,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Sinh mệnh dược','Bình máu huyền thoại.',0,5,-60,0,820,1900,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Đại sinh mệnh dược','Bình máu truyền thuyết.',0,6,-80,0,1200,3000,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Bình kinh nghiệm cao cấp','Tăng lượng kinh nghiệm cực nhiều.',0,8,0,15,30000,550000,NULL,NULL,NULL,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Mũ luyện ngục','Rèn trong lửa địa ngục.',2,8,NULL,NULL,6000,38000,150,10,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Giáp ngực luyện ngục','Rèn trong lửa địa ngục.',3,8,NULL,NULL,7900,56000,215,20,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Đao luyện ngục','Rèn trong lửa địa ngục.',4,8,NULL,NULL,8000,65000,0,142,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'IGăng luyện ngục','Rèn trong lửa địa ngục.',5,6,NULL,NULL,5800,35000,132,8,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Giày luyện ngục','Rèn trong lửa địa ngục.',6,6,NULL,NULL,5800,35000,132,8,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Khiên luyện ngục','Rèn trong lửa địa ngục.',8,5,NULL,NULL,7000,50000,180,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Lưỡi dao huyết ngọc','Dây chuyền chứa tinh thể đỏ như máu.',7,7,NULL,NULL,9999,99999,40,70,2,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Đá tảng chủ đạo','Tăng mạnh tập trung và chính xác.',7,8,NULL,NULL,30000,200000,100,60,10,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Khiên hấp thụ','Hấp thụ phần lớn sát thương.',8,7,NULL,NULL,8800,60000,200,0,0,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Cuồng nộ tử thần','Ban sức mạnh khủng khiếp.',9,5,NULL,NULL,16000,120000,14,90,2,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Vầng dương rực rỡ','Chiếu sáng hơn mọi nhẫn khác.',9,6,NULL,NULL,25000,170000,140,55,4,NULL);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Bùa cường hóa C','Tăng tỉ lệ cường hóa.',10,0,NULL,NULL,1000,10000,NULL,NULL,NULL,10);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Bùa cường hóa B','Tăng tỉ lệ cường hóa.',10,1,NULL,NULL,5000,60000,NULL,NULL,NULL,20);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Bùa cường hóa A','Tăng tỉ lệ cường hóa.',10,2,NULL,NULL,10000,130000,NULL,NULL,NULL,30);
INSERT INTO ShopItems(rarity,name,description,type,imgIndex,hp,exp,sell,price,mhp,dmg,acc,eChance) VALUES (3,'Bùa cường hóa S','Tăng tỉ lệ cường hóa.',10,3,NULL,NULL,20000,700000,NULL,NULL,NULL,40);
