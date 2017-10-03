public class Comment
{
    private String _title;
    private String _shopName;
    private String _shopLink;
    private String _shopAddress;
    private double _latitude;
    private double _longitude;
    private String _content;
    private double _evaluation;

    public Comment()
    {
    }

    //Copy constructor
    public Comment(Comment comment)
    {
        _title = comment.GetTitle();
        _shopName = comment.GetShopName();
        _shopLink = comment.GetShopLink();
        _shopAddress = comment.GetShopAddress();
        _latitude = comment.GetLatitude();
        _longitude = comment.GetLongitude();
        _content = comment.GetContent();
        _evaluation = comment.GetEvaluation();
    }

    public String GetTitle()
    {
        return _title;
    }

    public String GetShopName()
    {
        return _shopName;
    }

    public String GetShopLink()
    {
        return _shopLink;
    }

    public String GetShopAddress()
    {
        return _shopAddress;
    }

    public double GetLatitude()
    {
        return _latitude;
    }

    public double GetLongitude()
    {
        return _longitude;
    }

    public String GetContent()
    {
        return _content;
    }

    public double GetEvaluation()
    {
        return _evaluation;
    }

    public void SetTitle(String title)
    {
        _title = title;
    }

    public void SetShopName(String shopName)
    {
        _shopName = shopName;
    }

    public void SetShopLink(String shopLink)
    {
        _shopLink = shopLink;
    }

    public void SetShopAddress(String shopAddress)
    {
        _shopAddress = shopAddress;
    }

    public void SetLatitude(double latitude)
    {
        _latitude = latitude;
    }

    public void SetLongitude(double longitude)
    {
        _longitude = longitude;
    }

    public void SetContent(String content)
    {
        _content = content;
    }

    public void SetEvaluation(double evaluation)
    {
        _evaluation = evaluation;
    }
}