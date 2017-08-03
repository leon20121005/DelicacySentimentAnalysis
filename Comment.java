public class Comment
{
    private String _shopName;
    private String _shopLink;
    private String _shopAddress;
    private String _content;

    public Comment()
    {
    }

    //Copy constructor
    public Comment(Comment comment)
    {
        _shopName = comment.GetShopName();
        _shopLink = comment.GetShopLink();
        _shopAddress = comment.GetShopAddress();
        _content = comment.GetContent();
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

    public String GetContent()
    {
        return _content;
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

    public void SetContent(String content)
    {
        _content = content;
    }
}