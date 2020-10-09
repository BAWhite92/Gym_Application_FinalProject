
package gymbookingapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Ben
 */
class DateFormatHelper
{
    public static boolean dateFormat(String t){
    Date date = null;
    try{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        date = sdf.parse(t);
        if(!t.equals(sdf.format(date))){
            date = null;
        }
    } catch (ParseException e){
        e.printStackTrace();
    }
    return date != null;
    }

}
