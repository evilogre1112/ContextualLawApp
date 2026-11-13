import math

def Rranking(ListRelevant):
    scored_points = []
    #Phép biến đổi A(x,y)->A(sqrt(x**2 + xy/2),(y**2 + xy/2))
    #-> S'**2 = S**2 + xy
    #Phép biến đổi A(x,y,z)-> A(cbrt(x**3 + xyz/3),cbrt(y**3 + xyz/3),cbrt(z**3 + xyz/3))
    #-> S'**3 = S**3 + xyz 
    # Mỗi point là 1 điểm gồm toạ độ (x,y,z)
    for i, point in enumerate(ListRelevant) :
        valid = [val for val in point if val is not None]
        if(any (val < 0 for val in valid)): continue
        n = len(valid)
        if n == 0 :
            S = 0.0 
        elif n == 1 :
            S = float(valid[0])
        elif n == 2:
            a0, b0 = valid[0] ** 2, valid[1] ** 2
            T0 = a0 + b0
            G = 0.0
            for _ in range(5):
                G += 0.5 * math.sqrt((a0 + G) * (b0 + G))
            S = math.sqrt(T0 + 2 * G)/math.sqrt(2 * (1.5 ** 5))
        else:
            a0, b0, c0 = valid[0] ** 3, valid[1] ** 3, valid[2] ** 3
            T0 = a0 + b0 + c0
            G = 0.0
            for _ in range(8):
                G += (1 / 3) * math.cbrt((a0 + G) * (b0 + G) * (c0 + G))
            S = math.cbrt(T0 + 3 * G)/math.cbrt(3 * ((4 / 3) ** 8))
        scored_points.append((S,i))

    scored_points.sort(key=lambda item: item[0], reverse=True)
    Rrankings = [item[1] for item in scored_points]
    return Rrankings