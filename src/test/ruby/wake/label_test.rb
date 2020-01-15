require 'minitest/autorun'
require 'wake/label'

class LabelTest < Minitest::Test
  def test_parse_blank_path
    label = Wake::Label.parse('//:name')
    assert_equal 'name', label.path
  end

  def test_parse_path
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name', label.path
  end

  def test_parse_path_with_suffix
    label = Wake::Label.parse('//package:name')
    assert_equal 'package/name.suffix', label.path('suffix')
  end
end
